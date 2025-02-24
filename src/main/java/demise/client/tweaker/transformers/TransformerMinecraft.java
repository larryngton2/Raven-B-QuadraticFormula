package demise.client.tweaker.transformers;

import demise.client.tweaker.ASMTransformerClass;
import org.objectweb.asm.tree.*;

public class TransformerMinecraft implements Transformer {

   public String[] getClassName() {
      return new String[]{"net.minecraft.client.Minecraft"};
   }

   public void transform(ClassNode classNode, String transformedName) {
      for (Object m : classNode.methods) {
         String n = this.mapMethodName(classNode, (MethodNode) m);
         if (n.equalsIgnoreCase("runTick") || n.equalsIgnoreCase("func_71407_l")) {
            AbstractInsnNode[] arr = ((MethodNode) m).instructions.toArray();

            for (int i = 0; i < arr.length; ++i) {
               AbstractInsnNode ins = arr[i];
               if (i == 39) {
                  ((MethodNode) m).instructions.insert(ins, this.getEventInsn());
               } else if (i >= 40 && i <= 45) {
                  ((MethodNode) m).instructions.remove(ins);
               } else if (i == 46) {
                  return;
               }
            }

            return;
         }
      }

   }

   private InsnList getEventInsn() {
      InsnList insnList = new InsnList();
      insnList.add(new MethodInsnNode(184, ASMTransformerClass.eventHandlerClassName, "onTick", "()V", false));
      return insnList;
   }
}
