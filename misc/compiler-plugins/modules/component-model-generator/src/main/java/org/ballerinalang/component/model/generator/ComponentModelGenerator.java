package org.ballerinalang.component.model.generator;

import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.projects.plugins.CompilerPlugin;
import io.ballerina.projects.plugins.CompilerPluginContext;

public class ComponentModelGenerator extends CompilerPlugin {
    @Override
    public void init(CompilerPluginContext pluginContext) {
        pluginContext.addCodeAnalyzer(new ComponentModelAnalyzer());
    }

    public static class ComponentModelAnalyzer extends CodeAnalyzer {
        @Override
        public void init(CodeAnalysisContext analysisContext) {
            analysisContext.addCompilationAnalysisTask(new ComponentModelGenTask());
        }
    }
}
