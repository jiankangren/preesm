package org.ietr.preesm.codegen.xtend.model.codegen.util;

import org.ietr.preesm.codegen.xtend.model.codegen.CallBlock;
import org.ietr.preesm.codegen.xtend.model.codegen.CodegenFactory;
import org.ietr.preesm.codegen.xtend.model.codegen.CoreBlock;
import org.ietr.preesm.codegen.xtend.model.codegen.LoopBlock;

/**
 *
 * @author anmorvan
 *
 */
public class CodegenModelUserFactory {

  private CodegenModelUserFactory() {
    // Not meant to be instantiated: use static methods.
  }

  private static final CodegenFactory factory = CodegenFactory.eINSTANCE;

  /**
   *
   */
  public static final CoreBlock createCoreBlock() {
    final CoreBlock coreBlock = factory.createCoreBlock();
    final CallBlock initBlock = CodegenFactory.eINSTANCE.createCallBlock();
    final LoopBlock loopBlock = CodegenFactory.eINSTANCE.createLoopBlock();
    coreBlock.setInitBlock(initBlock);
    coreBlock.setLoopBlock(loopBlock);
    coreBlock.getCodeElts().add(initBlock);
    coreBlock.getCodeElts().add(loopBlock);
    return coreBlock;
  }

}
