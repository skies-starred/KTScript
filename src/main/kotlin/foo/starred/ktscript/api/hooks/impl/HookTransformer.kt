package foo.starred.ktscript.api.hooks.impl

import foo.starred.ktscript.api.hooks.context.HookContext
import foo.starred.ktscript.api.hooks.data.HookTarget
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.Method
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain

class HookTransformer : ClassFileTransformer {
    override fun transform(loader: ClassLoader?, className: String, classBeingRedefined: Class<*>?, protectionDomain: ProtectionDomain?, classfileBuffer: ByteArray): ByteArray? {
        val name = className.replace('/', '.')
        val targets = HookManager.targets.filter { it.klass == name }.takeIf { it.isNotEmpty() } ?: return null

        val reader = ClassReader(classfileBuffer)
        val writer = ClassWriter(reader, ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)

        reader.accept(visitor(writer, targets), ClassReader.EXPAND_FRAMES)
        return writer.toByteArray()
    }

    private fun visitor(writer: ClassWriter, targets: List<HookTarget>): ClassVisitor {
        return object : ClassVisitor(Opcodes.ASM9, writer) {
            override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
                val visitor = super.visitMethod(access, name, descriptor, signature, exceptions)
                val target = targets.firstOrNull { it.matches(name, descriptor) } ?: return visitor
                return advice(visitor, access, name, descriptor, target.key)
            }
        }
    }

    private fun advice(visitor: MethodVisitor, access: Int, name: String, descriptor: String, key: String): AdviceAdapter {
        val static = (access and Opcodes.ACC_STATIC) != 0

        return object : AdviceAdapter(ASM9, visitor, access, name, descriptor) {
            override fun onMethodEnter() {
                push(key)
                if (static) push(null as String?) else loadThis()
                loadArgArray()

                invokeStatic(Type.getType(HookDispatcher::class.java), Method("enter", "(Ljava/lang/String;Ljava/lang/Object;[Ljava/lang/Object;)Lfoo/starred/ktscript/api/hooks/context/HookContext;"))

                val label = Label()
                dup()
                ifNull(label)

                dup()
                getField(Type.getType(HookContext::class.java), "cancelled", Type.BOOLEAN_TYPE)
                ifZCmp(EQ, label)

                pop()
                when (returnType.sort) {
                    Type.VOID -> {
                        visitor.visitInsn(RETURN)
                    }

                    Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT, Type.INT -> {
                        push(0)
                        returnValue()
                    }

                    Type.LONG -> {
                        push(0L)
                        returnValue()
                    }

                    Type.FLOAT -> {
                        push(0f)
                        returnValue()
                    }

                    Type.DOUBLE -> {
                        push(0.0)
                        returnValue()
                    }

                    else -> {
                        push(null as String?)
                        returnValue()
                    }
                }

                mark(label)
                pop()
            }

            override fun onMethodExit(opcode: Int) {
                if (opcode == ATHROW) return

                push(key)
                if (static) push(null as String?) else loadThis()
                loadArgArray()
                push(null as String?)

                invokeStatic(Type.getType(HookDispatcher::class.java), Method("exit", "(Ljava/lang/String;Ljava/lang/Object;[Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
                pop()
            }
        }
    }
}
