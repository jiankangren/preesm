package org.ietr.preesm.codegen.xtend.printer.net.c;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.ietr.preesm.codegen.xtend.CodegenPlugin;
import org.ietr.preesm.codegen.xtend.model.codegen.Block;
import org.ietr.preesm.codegen.xtend.model.codegen.CallBlock;
import org.ietr.preesm.codegen.xtend.model.codegen.CoreBlock;
import org.ietr.preesm.codegen.xtend.model.codegen.Delimiter;
import org.ietr.preesm.codegen.xtend.model.codegen.Direction;
import org.ietr.preesm.codegen.xtend.model.codegen.LoopBlock;
import org.ietr.preesm.codegen.xtend.model.codegen.SharedMemoryCommunication;
import org.ietr.preesm.codegen.xtend.model.codegen.Variable;
import org.ietr.preesm.codegen.xtend.printer.c.CPrinter;
import org.ietr.preesm.codegen.xtend.task.CodegenException;
import org.ietr.preesm.utils.files.URLResolver;

/**
 *
 * @author anmorvan
 *
 */
public class TcpCPrinter extends CPrinter {

  @Override
  public CharSequence printDeclarationsHeader(List<Variable> list) {
    return "";
  }

  @Override
  public CharSequence printCoreInitBlockHeader(CallBlock callBlock) {
    final int coreID = ((CoreBlock) callBlock.eContainer()).getCoreID();
    StringBuilder ff = new StringBuilder();
    ff.append("#include \"socketcom.h\"\n");
    ff.append("void *computationThread_Core");
    ff.append(coreID);
    ff.append("(void *arg) {\n");

    ff.append("  int* socketFileDescriptors = (int*)arg;\n");
    ff.append("  int processingElementID = socketFileDescriptors[" + this.getEngine().getCodeBlocks().size() + "];\n");

    // ff.append(" int* socketFileDescriptors = (int*)arg;\n");

    ff.append("  \n" + "#ifdef _PREESM_TCP_DEBUG_\n" + "  printf(\"[TCP-DEBUG] Core" + coreID + " READY\\n\");\n" + "#endif\n\n");
    return ff.toString();
  }

  @Override
  public CharSequence printCoreLoopBlockHeader(LoopBlock block2) {
    final CoreBlock eContainer = (CoreBlock) block2.eContainer();

    final int coreID = eContainer.getCoreID();
    StringBuilder res = new StringBuilder();
    res.append("  int iterationCount = 0;\n");
    res.append("  while(1){\n");
    res.append("    iterationCount++;\n");
    res.append("#ifdef _PREESM_TCP_DEBUG_\n" + "    printf(\"[TCP-DEBUG] Core" + coreID + " iteration #%d - at barrier\\n\",iterationCount);\n" + "#endif\n");
    res.append("    preesm_barrier(socketFileDescriptors, " + coreID + ", " + this.getEngine().getCodeBlocks().size() + ");\n");
    res.append("#ifdef _PREESM_TCP_DEBUG_\n" + "    printf(\"[TCP-DEBUG] Core" + coreID + " iteration #%d - barrier passed\\n\",iterationCount);\n"
        + "#endif\n    \n    ");
    return res.toString();
  }

  @Override
  public CharSequence printSharedMemoryCommunication(SharedMemoryCommunication communication) {
    final StringBuilder functionCallBuilder = new StringBuilder("preesm_");

    final Direction direction = communication.getDirection();
    final int to = communication.getReceiveEnd().getCoreContainer().getCoreID();
    final int from = communication.getSendStart().getCoreContainer().getCoreID();
    switch (direction) {
      case SEND:
        functionCallBuilder.append("send_");
        break;
      case RECEIVE:
        functionCallBuilder.append("receive_");
        break;
      default:
        throw new UnsupportedOperationException("Unsupported [" + direction + "] communication direction.");
    }

    final Delimiter delimiter = communication.getDelimiter();
    switch (delimiter) {
      case START:
        functionCallBuilder.append("start");
        break;
      case END:
        functionCallBuilder.append("end");
        break;
      default:
        throw new UnsupportedOperationException("Unsupported [" + direction + "] communication direction.");
    }
    final int size = communication.getData().getSize();

    final String dataAddress = communication.getData().getName();

    functionCallBuilder
        .append("(" + from + ", " + to + ", socketFileDescriptors, " + dataAddress + ", " + size + ", \"" + dataAddress + " " + size + "\"" + ");\n");

    return functionCallBuilder.toString();
  }

  @Override
  public String printMain(final List<Block> printerBlocks) {
    // 0- without the following class loader initialization, I get the following exception when running as Eclipse plugin:
    // org.apache.velocity.exception.VelocityException: The specified class for ResourceManager
    // (org.apache.velocity.runtime.resource.ResourceManagerImpl) does not implement
    // org.apache.velocity.runtime.resource.ResourceManager; Velocity is not initialized correctly.
    final ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(TcpCPrinter.class.getClassLoader());

    // 1- init engine
    final VelocityEngine engine = new VelocityEngine();
    engine.init();

    // 2- init context
    final VelocityContext context = new VelocityContext();
    context.put("PREESM_DATE", new Date().toString());
    context.put("PREESM_PRINTER", this.getClass().getSimpleName());
    context.put("PREESM_NBTHREADS", printerBlocks.size());

    final List<String> threadFunctionNames = IntStream.range(0, printerBlocks.size()).mapToObj(i -> String.format("computationThread_Core%d", i))
        .collect(Collectors.toList());

    context.put("PREESM_THREAD_FUNCTIONS_DECLS", "void* " + String.join("(void *arg);\nvoid* ", threadFunctionNames) + "(void *arg);\n");

    context.put("PREESM_THREAD_FUNCTIONS", "&" + String.join(",&", threadFunctionNames));

    // 3- init template reader
    final String templateLocalURL = "templates/tcp/main.c";
    final URL mainTemplate = URLResolver.findFirstInPluginList(templateLocalURL, CodegenPlugin.BUNDLE_ID);
    InputStreamReader reader = null;
    try {
      reader = new InputStreamReader(mainTemplate.openStream());
    } catch (IOException e) {
      throw new CodegenException("Could not locate main template [" + templateLocalURL + "].", e);
    }

    // 4- init output writer
    StringWriter writer = new StringWriter();

    engine.evaluate(context, writer, "org.apache.velocity", reader);

    // 99- set back default class loader
    Thread.currentThread().setContextClassLoader(oldContextClassLoader);

    return writer.getBuffer().toString();
  }
}