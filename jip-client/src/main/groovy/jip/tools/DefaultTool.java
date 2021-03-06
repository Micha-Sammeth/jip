package jip.tools;

import groovy.lang.Closure;
import jip.dsl.JipDSL;
import jip.dsl.JipDSLContext;
import jip.graph.*;
import jip.jobs.Job;
import jip.utils.ExecuteDelegate;

import java.io.File;
import java.util.*;

/**
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
public class DefaultTool implements Tool {
    private JipDSLContext context;
    private String description;
    private String name;
    private Map<String, Parameter> parameter;
    private String defaultInput;
    private String defaultOutput;
    private List<String> installer;
    private Closure closure;
    private Closure pipeline;
    private String version;
    private Closure args;
    private ExecuteEnvironment executeEnvironment;

    /**
     * Create a new but empty tool
     *
     * @param name the tool name
     */
    public DefaultTool(String name) {
        this(name, null);
    }

    /**
     * Create a new tool with a reference to the JIP context.
     * This is needed to evaluate and run pipelines
     *
     * @param name the tool name
     * @param context the context
     */
    public DefaultTool(String name, JipDSLContext context) {
        this.name = name;
        this.context = context;
        this.executeEnvironment = new DefaultExecuteEnvironment();
    }

    @Override
    public String getName() {
        return name; 
    }
    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }


    @Override
    public void run(File cwd, Map cfg, Job job) throws Exception {
        // make file paths absolute
        if(cfg != null && cfg.size() > 0){
            if(cwd == null) cwd = new File(".");
            cfg = absoluteFileParameter(cwd, cfg);
        }
        if(pipeline == null){
            ExecuteDelegate delegate = new ExecuteDelegate(cwd, true, job);
            delegate.setTemplateConfiguration(this, cfg);
            closure.setDelegate(delegate);
            closure.call(cfg);
        }else{
            if(context == null){
                throw new NullPointerException("No JIP context specified! Unable to evaluate and run pipelines");
            }
            // run pipeline
            Pipeline pipeline = new JipDSL(context).evaluateRun(cfg, this.pipeline);
            PipelineGraph graph = new PipelineGraph(pipeline);
            graph.prepare();
            graph.reduceDependencies();
            for (JobNode node : graph.getNodes()) {
                PipelineJob pipelineJob = node.getPipelineJob();
                String tool = pipelineJob.getToolId();
                Tool jobTool = context.getTools().get(tool);
                if(jobTool == null) throw new NullPointerException("Tool " + tool + " not found");
                jobTool.run(cwd, node.getConfiguration(), job);
            }
        }
    }

    /**
     * Make all file parameter values absolute with respect
     * to the given directory
     *
     * @param cwd the working directory
     * @param cfg the configuration
     * @return config copy of the configuration with absolute paths
     */
    Map absoluteFileParameter(File cwd, Map cfg) {
        Map copy = new HashMap(cfg);
        for (Parameter parameter : getParameter().values()) {
            if(parameter.isFile() && cfg.containsKey(parameter.getName())){
                if(parameter.isList()){
                    // convert list
                    ArrayList values = new ArrayList();
                    for (Object element : ((Collection)cfg.get(parameter.getName()))) {
                        values.add(toAbsolute(cwd, element.toString()));
                    }
                    copy.put(parameter.getName(), values);
                }else{
                    // single entry
                    copy.put(parameter.getName(), toAbsolute(cwd, copy.get(parameter.getName()).toString()));
                }
            }
        }
        return copy;
    }

    FileParameter toAbsolute(File cwd, String path) {

        if(path.startsWith("/")) return new FileParameter(path);
        return new FileParameter(new File(cwd, path).getAbsolutePath());
    }

    @Override
    public Map<String, Parameter> getParameter() {
        if(parameter == null){
            parameter = new HashMap<String, Parameter>();
        }
        return parameter;
    }

    @Override
    public String getDefaultInput() {
        return defaultInput;
    }

    @Override
    public String getDefaultOutput() {
        return defaultOutput;
    }

    @Override
    public List<String> getInstaller() {
        return installer;
    }

    @Override
    public ExecuteEnvironment getExecuteEnvironment() {
        return executeEnvironment;
    }

    public void setExecuteEnvironment(ExecuteEnvironment executeEnvironment) {
        this.executeEnvironment = executeEnvironment;
    }

    public Closure getArgs() {
        return args;
    }

    public Closure getPipeline() {
        return pipeline;
    }

    public void setPipeline(Closure pipeline) {
        this.pipeline = pipeline;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParameter(Map<String, Parameter> parameter) {
        this.parameter = parameter;
    }

    public void setDefaultInput(String defaultInput) {
        this.defaultInput = defaultInput;
    }

    public void setDefaultOutput(String defaultOutput) {
        this.defaultOutput = defaultOutput;
    }

    public void setInstaller(List<String> installer) {
        this.installer = installer;
    }

    public void setClosure(Closure closure) {
        this.closure = closure;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setArgs(Closure args) {
        this.args = args;
    }

    public JipContext getContext() {
        return context;
    }

    public void setContext(JipDSLContext context) {
        this.context = context;
    }
}
