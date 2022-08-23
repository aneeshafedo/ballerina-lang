package org.ballerinalang.component.model.generator;

import io.ballerina.projects.plugins.*;

public class ComponentModelGenerator extends CompilerPlugin {
    @Override
    public void init(CompilerPluginContext pluginContext) {
        pluginContext.addCodeAnalyzer(new ComponentModelAnalyzer());
    }

//    public static class ComponentModelLifeCycleListener extends CompilerLifecycleListener {
//        @Override
//        public void init(CompilerLifecycleContext compilerLifecycleContext) {
//            compilerLifecycleContext.addCodeGenerationCompletedTask(new ComponentModelGenTask());
//        }
//    }
    public static class ComponentModelAnalyzer extends CodeAnalyzer {
        @Override
        public void init(CodeAnalysisContext codeAnalysisContext) {
            codeAnalysisContext.addCompilationAnalysisTask(new ComponentModelGenTask());
        }
    }
}
