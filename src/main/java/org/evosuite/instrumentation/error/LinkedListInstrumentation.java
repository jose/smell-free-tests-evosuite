package org.evosuite.instrumentation.error;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.evosuite.instrumentation.ErrorConditionMethodAdapter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class LinkedListInstrumentation extends ErrorBranchInstrumenter {

	private static final String LISTNAME = LinkedList.class.getCanonicalName().replace('.', '/');
	
	private final List<String> emptyListMethods = Arrays.asList(new String[] {"getFirst", "getLast", "removeFirst", "removeLast", "element", "pop"});

	private final List<String> indexListMethods = Arrays.asList(new String[] {"get", "set", "add", "remove", "listIterator", "addAll"});

	public LinkedListInstrumentation(ErrorConditionMethodAdapter mv) {
		super(mv);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {
		if(owner.equals(LISTNAME)) {
			if(emptyListMethods.contains(name)) {
				// empty
				Map<Integer, Integer> tempVariables = getMethodCallee(desc);

				tagBranchStart();
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, LISTNAME,
	                      "isEmpty", "()Z");
				insertBranchWithoutTag(Opcodes.IFLE, "java/util/NoSuchElementException");
				tagBranchEnd();
				restoreMethodParameters(tempVariables, desc);
				
			} else if(indexListMethods.contains(name)) {
				Type[] args = Type.getArgumentTypes(desc);
				if(args.length == 0)
					return;
				if(!args[0].equals(Type.INT_TYPE))
					return;
				
				Map<Integer, Integer> tempVariables = getMethodCallee(desc);
				tagBranchStart();
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, LISTNAME,
	                      "size", "()I");
				
				// index >= size
				mv.loadLocal(tempVariables.get(0));
				insertBranch(Opcodes.IF_ICMPGT, "java/lang/IndexOutOfBoundsException");
					
				// index < 0
				mv.loadLocal(tempVariables.get(0));
				insertBranch(Opcodes.IFGE, "java/lang/IndexOutOfBoundsException");
				tagBranchEnd();

				restoreMethodParameters(tempVariables, desc);
			}
		}
	}
	
	
}