package keystrokesmod.client.tweaker.transformers;

import keystrokesmod.client.tweaker.ASMTransformerClass;
import org.objectweb.asm.tree.*;

public class TransformerEntityPlayer implements Transformer {
   public String[] getClassName() {
      return new String[]{"net.minecraft.entity.player.EntityPlayer"};
   }

   public void transform(ClassNode classNode, String transformedName) {
      for (Object methodNode : classNode.methods) {
         String mappedMethodName = this.mapMethodName(classNode, (MethodNode) methodNode);
         if (mappedMethodName.equalsIgnoreCase("attackTargetEntityWithCurrentItem") || mappedMethodName.equalsIgnoreCase("func_71059_n")) {
            AbstractInsnNode[] arr = ((MethodNode) methodNode).instructions.toArray();

            for (int i = 0; i < arr.length; ++i) {
               AbstractInsnNode ins = arr[i];
               if (i == 232) {
                  ((MethodNode) methodNode).instructions.insert(ins, this.h());
               } else if (i >= 233 && i <= 248) {
                  ((MethodNode) methodNode).instructions.remove(ins);
               } else if (i == 249) {
                  return;
               }
            }

            return;
         }
      }

   }

   private InsnList h() {
      InsnList insnList = new InsnList();
      insnList.add(new VarInsnNode(25, 1));
      insnList.add(new MethodInsnNode(184, ASMTransformerClass.eventHandlerClassName, "onAttackTargetEntityWithCurrentItem", "(Lnet/minecraft/entity/Entity;)V", false));
      return insnList;
   }
}
