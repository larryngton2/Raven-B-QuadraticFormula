package keystrokesmod.client.tweaker.transformers;

import keystrokesmod.client.tweaker.ASMTransformerClass;
import org.objectweb.asm.tree.*;

public class TransformerEntity implements Transformer {
   public String[] getClassName() {
      return new String[]{"net.minecraft.entity.Entity"};
   }

   public void transform(ClassNode classNode, String transformedName) {
      for (Object methodNode : classNode.methods) {
         String n = this.mapMethodName(classNode, (MethodNode) methodNode);
         if (n.equalsIgnoreCase("moveEntity") || n.equalsIgnoreCase("func_70091_d")) {
            AbstractInsnNode[] arr = ((MethodNode) methodNode).instructions.toArray();

            for (int i = 0; i < arr.length; ++i) {
               AbstractInsnNode ins = arr[i];
               if (i >= 99 && i <= 117) {
                  ((MethodNode) methodNode).instructions.remove(ins);
               } else if (i == 118) {
                  ((MethodNode) methodNode).instructions.insertBefore(ins, this.getEventInsn());
                  return;
               }
            }

            return;
         }
      }

   }

   private InsnList getEventInsn() {
      InsnList insnList = new InsnList();
      insnList.add(new VarInsnNode(25, 0));
      insnList.add(new MethodInsnNode(184, ASMTransformerClass.eventHandlerClassName, "onEntityMove", "(Lnet/minecraft/entity/Entity;)Z", false));
      insnList.add(new VarInsnNode(54, 19));
      return insnList;
   }
}
