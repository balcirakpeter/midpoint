/*
 * Copyright (c) 2012 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2012 [name of copyright owner]
 */

package com.evolveum.midpoint.wf;

import com.evolveum.midpoint.model.api.context.ModelContext;
import com.evolveum.midpoint.model.controller.ModelOperationTaskHandler;
import com.evolveum.midpoint.model.lens.LensContext;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.repo.api.RepositoryService;
import com.evolveum.midpoint.schema.DeltaConvertor;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.wf.processors.primary.PrimaryApprovalProcessWrapper;
import com.evolveum.midpoint.wf.processors.ChangeProcessor;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.*;
import com.evolveum.midpoint.xml.ns._public.communication.workflow_1.WfProcessInstanceEventType;
import com.evolveum.midpoint.xml.ns._public.communication.workflow_1.WfProcessVariable;
import com.evolveum.midpoint.xml.ns._public.model.model_context_2.LensContextType;
import com.evolveum.prism.xml.ns._public.types_2.ObjectDeltaType;
import com.evolveum.prism.xml.ns._public.types_2.PolyStringType;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * Handles low-level task operations, e.g. handling wf* properties in task extension.
 *
 * @author mederly
 *
 */

@Component
@DependsOn({ "prismContext" })

public class WfTaskUtil {

    @Autowired(required = true)
    private RepositoryService repositoryService;

    @Autowired(required = true)
    private PrismContext prismContext;

    @Autowired
    private WfConfiguration wfConfiguration;

	private static final Trace LOGGER = TraceManager.getTrace(WfTaskUtil.class);

    public static final String WORKFLOW_EXTENSION_NS = "http://midpoint.evolveum.com/model/workflow/extension-2";

    // wfModelContext - records current model context (i.e. context of current model operation)

    private PrismPropertyDefinition wfModelContextPropertyDefinition;
    private PrismPropertyDefinition wfSkipModelContextProcessingPropertyDefinition;

    // wfStatus - records information about process execution at WfMS
    // for "smart" processes it is a user-defined message; for dump ones it is usually simple "process instance has proceeded further"

	public static final QName WFSTATUS_PROPERTY_NAME = new QName(WORKFLOW_EXTENSION_NS, "status");
	private PrismPropertyDefinition wfStatusPropertyDefinition;

    // wfLastDetails - records detailed information about process instance, currently generated by the PrimaryApprovalProcessWrapper
    // (e.g. for Add Role approval process there is a list of decisions done up to now)

    public static final QName WFLAST_DETAILS_PROPERTY_NAME = new QName(WORKFLOW_EXTENSION_NS, "lastDetails");
    private PrismPropertyDefinition wfLastDetailsPropertyDefinition;

	// wfLastVariables - stores dump of lastly received process instance variables

	public static final QName WFLASTVARIABLES_PROPERTY_NAME = new QName(WORKFLOW_EXTENSION_NS, "lastVariables");
	private PrismPropertyDefinition wfLastVariablesPropertyDefinition;

	// wfProcessWrapper - class that serves as an interface for starting and finishing wf process
	
	public static final QName WFPROCESS_WRAPPER_PROPERTY_NAME = new QName(WORKFLOW_EXTENSION_NS, "processWrapper");
	private PrismPropertyDefinition wfProcessWrapperPropertyDefinition;

    // wfChangeProcessor - ...

    public static final QName WFCHANGE_PROCESSOR_PROPERTY_NAME = new QName(WORKFLOW_EXTENSION_NS, "changeProcessor");
    private PrismPropertyDefinition wfChangeProcessorPropertyDefinition;

    // wfProcessId - id of the workflow process instance
	
	public static final QName WFPROCESSID_PROPERTY_NAME = new QName(WORKFLOW_EXTENSION_NS, "processInstanceId");
	private PrismPropertyDefinition wfProcessIdPropertyDefinition;

    // wfDeltaToProcess

    public static final QName WFDELTA_TO_PROCESS_PROPERTY_NAME = new QName(WORKFLOW_EXTENSION_NS, "deltaToProcess");
    private PrismPropertyDefinition wfDeltaToProcessPropertyDefinition;

    // wfResultingDelta

    public static final QName WFRESULTING_DELTA_PROPERTY_NAME = new QName(WORKFLOW_EXTENSION_NS, "resultingDelta");
    private PrismPropertyDefinition wfResultingDeltaPropertyDefinition;

    // processInstanceFinished

    public static final QName WFPROCESS_INSTANCE_FINISHED_PROPERTY_NAME = new QName(WORKFLOW_EXTENSION_NS, "processInstanceFinished");
    private PrismPropertyDefinition wfProcessInstanceFinishedPropertyDefinition;

    @PostConstruct
    public void init() {

        wfModelContextPropertyDefinition = prismContext.getSchemaRegistry().findPropertyDefinitionByElementName(ModelOperationTaskHandler.MODEL_CONTEXT_PROPERTY);
        wfSkipModelContextProcessingPropertyDefinition = prismContext.getSchemaRegistry().findPropertyDefinitionByElementName(ModelOperationTaskHandler.SKIP_MODEL_CONTEXT_PROCESSING_PROPERTY);
        wfStatusPropertyDefinition = prismContext.getSchemaRegistry().findPropertyDefinitionByElementName(WFSTATUS_PROPERTY_NAME);
        wfLastDetailsPropertyDefinition = prismContext.getSchemaRegistry().findPropertyDefinitionByElementName(WFLAST_DETAILS_PROPERTY_NAME);
        wfLastVariablesPropertyDefinition = prismContext.getSchemaRegistry().findPropertyDefinitionByElementName(WFLASTVARIABLES_PROPERTY_NAME);
		wfProcessWrapperPropertyDefinition = prismContext.getSchemaRegistry().findPropertyDefinitionByElementName(WFPROCESS_WRAPPER_PROPERTY_NAME);
        wfChangeProcessorPropertyDefinition = prismContext.getSchemaRegistry().findPropertyDefinitionByElementName(WFCHANGE_PROCESSOR_PROPERTY_NAME);
		wfProcessIdPropertyDefinition = prismContext.getSchemaRegistry().findPropertyDefinitionByElementName(WFPROCESSID_PROPERTY_NAME);
        wfDeltaToProcessPropertyDefinition = prismContext.getSchemaRegistry().findPropertyDefinitionByElementName(WFDELTA_TO_PROCESS_PROPERTY_NAME);
        wfResultingDeltaPropertyDefinition = prismContext.getSchemaRegistry().findPropertyDefinitionByElementName(WFRESULTING_DELTA_PROPERTY_NAME);
        wfProcessInstanceFinishedPropertyDefinition = prismContext.getSchemaRegistry().findPropertyDefinitionByElementName(WFPROCESS_INSTANCE_FINISHED_PROPERTY_NAME);

        Validate.notNull(wfModelContextPropertyDefinition, ModelOperationTaskHandler.MODEL_CONTEXT_PROPERTY + " definition was not found");
        Validate.notNull(wfSkipModelContextProcessingPropertyDefinition, ModelOperationTaskHandler.SKIP_MODEL_CONTEXT_PROCESSING_PROPERTY + " definition was not found");
        Validate.notNull(wfStatusPropertyDefinition, WFSTATUS_PROPERTY_NAME + " definition was not found");
        Validate.notNull(wfLastDetailsPropertyDefinition, WFLAST_DETAILS_PROPERTY_NAME + " definition was not found");
        Validate.notNull(wfLastVariablesPropertyDefinition, WFLASTVARIABLES_PROPERTY_NAME + " definition was not found");
        Validate.notNull(wfProcessWrapperPropertyDefinition, WFPROCESS_WRAPPER_PROPERTY_NAME + " definition was not found");
        Validate.notNull(wfChangeProcessorPropertyDefinition, WFCHANGE_PROCESSOR_PROPERTY_NAME + " definition was not found");
        Validate.notNull(wfProcessIdPropertyDefinition, WFPROCESSID_PROPERTY_NAME + " definition was not found");
        Validate.notNull(wfDeltaToProcessPropertyDefinition, WFDELTA_TO_PROCESS_PROPERTY_NAME + " definition was not found");
        Validate.notNull(wfResultingDeltaPropertyDefinition, WFRESULTING_DELTA_PROPERTY_NAME + " definition was not found");
        Validate.notNull(wfProcessInstanceFinishedPropertyDefinition, WFPROCESS_INSTANCE_FINISHED_PROPERTY_NAME + " definition was not found");

        if (wfLastVariablesPropertyDefinition.isIndexed() != Boolean.FALSE) {
            throw new SystemException("lastVariables property isIndexed attribute is incorrect (should be FALSE, it is " + wfLastVariablesPropertyDefinition.isIndexed() + ")");
        }
        if (wfLastDetailsPropertyDefinition.isIndexed() != Boolean.FALSE) {
            throw new SystemException("lastDetails property isIndexed attribute is incorrect (should be FALSE, it is " + wfLastDetailsPropertyDefinition.isIndexed() + ")");
        }

    }

	void setWfProcessIdImmediate(Task task, String pid, OperationResult parentResult) throws SchemaException, ObjectNotFoundException {
		String oid = task.getOid();
		Validate.notEmpty(oid, "Task oid must not be null or empty (task must be persistent).");
		setExtensionPropertyImmediate(task, wfProcessIdPropertyDefinition, pid, parentResult);
	}

	String getLastVariables(Task task) {
		PrismProperty<?> p = task.getExtension(WFLASTVARIABLES_PROPERTY_NAME);
		if (p == null) {
			return null;
        } else {
			return p.getValue(String.class).getValue();
        }
	}

    String getLastDetails(Task task) {
        PrismProperty<?> p = task.getExtension(WFLAST_DETAILS_PROPERTY_NAME);
        if (p == null) {
            return null;
        } else {
            return p.getValue(String.class).getValue();
        }
    }

    /**
	 * Sets an extension property.
	 */
	private<T> void setExtensionPropertyImmediate(Task task, PrismPropertyDefinition propertyDef, T propertyValue, OperationResult parentResult) throws SchemaException, ObjectNotFoundException {
		if (parentResult == null)
			parentResult = new OperationResult("setExtensionPropertyImmediate");
		
		PrismProperty prop = propertyDef.instantiate();
        prop.setValue(new PrismPropertyValue<T>(propertyValue));

		try {
            task.setExtensionPropertyImmediate(prop, parentResult);
        } catch (ObjectNotFoundException ex) {
			parentResult.recordFatalError("Object not found", ex);
            LoggingUtils.logException(LOGGER, "Cannot set {} for task {}", ex, propertyDef.getName(), task);
            throw ex;
		} catch (SchemaException ex) {
			parentResult.recordFatalError("Schema error", ex);
            LoggingUtils.logException(LOGGER, "Cannot set {} for task {}", ex, propertyDef.getName(), task);
            throw ex;
		} catch (RuntimeException ex) {
			parentResult.recordFatalError("Internal error", ex);
            LoggingUtils.logException(LOGGER, "Cannot set {} for task {}", ex, propertyDef.getName(), task);
            throw ex;
		}
		
	}

    private<T> void setExtensionProperty(Task task, PrismPropertyDefinition propertyDef, T propertyValue, OperationResult parentResult) throws SchemaException, ObjectNotFoundException {
        if (parentResult == null)
            parentResult = new OperationResult("setExtensionProperty");

        PrismProperty prop = propertyDef.instantiate();
        prop.setValue(new PrismPropertyValue<T>(propertyValue));

        try {
            task.setExtensionProperty(prop);
        } catch (SchemaException ex) {
            parentResult.recordFatalError("Schema error", ex);
            LoggingUtils.logException(LOGGER, "Cannot set {} for task {}", ex, propertyDef.getName(), task);
            throw ex;
        } catch (RuntimeException ex) {
            parentResult.recordFatalError("Internal error", ex);
            LoggingUtils.logException(LOGGER, "Cannot set {} for task {}", ex, propertyDef.getName(), task);
            throw ex;
        }

    }

    // todo: fix this brutal hack (task closing should not go through repo)
	void markTaskAsClosed(Task task, OperationResult parentResult) throws ObjectNotFoundException, SchemaException {
        String oid = task.getOid();
		try {
            ItemDelta delta = PropertyDelta.createReplaceDelta(
                    prismContext.getSchemaRegistry().findObjectDefinitionByCompileTimeClass(TaskType.class),
                    TaskType.F_EXECUTION_STATUS,
                    TaskExecutionStatusType.CLOSED);
            List<ItemDelta> deltas = new ArrayList<ItemDelta>();
            deltas.add(delta);
			repositoryService.modifyObject(TaskType.class, oid, deltas, parentResult);
		} catch (ObjectNotFoundException e) {
            LoggingUtils.logException(LOGGER, "Cannot mark task " + task + " as closed.", e);
            throw e;
        } catch (SchemaException e) {
            LoggingUtils.logException(LOGGER, "Cannot mark task " + task + " as closed.", e);
            throw e;
        } catch (ObjectAlreadyExistsException e) {
            LoggingUtils.logException(LOGGER, "Cannot mark task " + task + " as closed.", e);
            throw new SystemException(e);
        }

    }

//	void returnToAsyncCaller(Task task, OperationResult parentResult) throws Exception {
//		try {
//			task.finishHandler(parentResult);
////			if (task.getExclusivityStatus() == TaskExclusivityStatus.CLAIMED) {
////				taskManager.releaseTask(task, parentResult);	// necessary for active tasks
////				task.shutdown();								// this works when this java object is the same as is being executed by CycleRunner
////			}
////			if (task.getHandlerUri() != null)
////				task.setExecutionStatusWithPersist(TaskExecutionStatus.RUNNING, parentResult);
//		}
//		catch (Exception e) {
//			throw new Exception("Couldn't mark task " + task + " as running.", e);
//		}
//	}

//	void setTaskResult(String oid, OperationResult result) throws Exception {
//		try {
//			OperationResultType ort = result.createOperationResultType();
//			repositoryService.modifyObject(TaskType.class, ObjectTypeUtil.createModificationReplaceProperty(oid, 
//				SchemaConstants.C_RESULT,
//				ort), result);
//		}
//		catch (Exception e) {
//			throw new Exception("Couldn't set result for the task " + oid, e);
//		}
//
//	}


//    void putWorkflowAnswerIntoTask(boolean doit, Task task, OperationResult result) throws IOException, ClassNotFoundException {
//
//    	ModelOperationStateType state = task.getModelOperationState();
//    	if (state == null)
//    		state = new ModelOperationStateType();
//
////    	state.setShouldBeExecuted(doit);
////    	task.setModelOperationStateWithPersist(state, result);
//    }

    public void setProcessWrapper(Task task, PrimaryApprovalProcessWrapper wrapper) throws SchemaException {
        Validate.notNull(wrapper, "Process Wrapper is undefined.");
        PrismProperty<String> w = wfProcessWrapperPropertyDefinition.instantiate();
        w.setRealValue(wrapper.getClass().getName());
        task.setExtensionProperty(w);
    }

    public PrimaryApprovalProcessWrapper getProcessWrapper(Task task, List<PrimaryApprovalProcessWrapper> wrappers) {
        String wrapperClassName = getExtensionValue(String.class, task, WFPROCESS_WRAPPER_PROPERTY_NAME);
        if (wrapperClassName == null) {
            throw new IllegalStateException("No wf process wrapper defined in task " + task);
        }

        for (PrimaryApprovalProcessWrapper w : wrappers) {
            if (wrapperClassName.equals(w.getClass().getName())) {
                return w;
            }
        }

        throw new IllegalStateException("Wrapper " + wrapperClassName + " is not registered.");
    }

    public void setChangeProcessor(Task task, ChangeProcessor processor) throws SchemaException {
        Validate.notNull(processor, "Change processor is undefined.");
        PrismProperty<String> w = wfChangeProcessorPropertyDefinition.instantiate();
        w.setRealValue(processor.getClass().getName());
        task.setExtensionProperty(w);
    }

    public ChangeProcessor getChangeProcessor(Task task) {
        String processorClassName = getExtensionValue(String.class, task, WFCHANGE_PROCESSOR_PROPERTY_NAME);
        if (processorClassName == null) {
            throw new IllegalStateException("No change processor defined in task " + task);
        }
        return wfConfiguration.findChangeProcessor(processorClassName);
    }


    public Map<String, String> unwrapWfVariables(WfProcessInstanceEventType event) {
        Map<String,String> retval = new HashMap<String,String>();
        for (WfProcessVariable var : event.getWfProcessVariable()) {
            retval.put(var.getName(), var.getValue());
        }
        return retval;
    }

    public String getWfVariable(WfProcessInstanceEventType event, String name) {
        for (WfProcessVariable v : event.getWfProcessVariable())
            if (name.equals(v.getName()))
                return v.getValue();
        return null;
    }


//    public void markRejection(Task task, OperationResult result) throws ObjectNotFoundException, SchemaException {
//
//        ModelOperationStateType state = task.getModelOperationState();
//        state.setStage(null);
//        task.setModelOperationState(state);
//        try {
//            task.savePendingModifications(result);
//        } catch (ObjectAlreadyExistsException ex) {
//            throw new SystemException(ex);
//        }
//
//        markTaskAsClosed(task, result);         // brutal hack
//    }


//    public void markAcceptation(Task task, OperationResult result) throws ObjectNotFoundException, SchemaException {
//
//        ModelOperationStateType state = task.getModelOperationState();
//        ModelOperationStageType stage = state.getStage();
//        switch (state.getStage()) {
//            case PRIMARY: stage = ModelOperationStageType.SECONDARY; break;
//            case SECONDARY: stage = ModelOperationStageType.EXECUTE; break;
//            case EXECUTE: stage = null; break;      // should not occur
//            default: throw new SystemException("Unknown model operation stage: " + state.getStage());
//        }
//        state.setStage(stage);
//        task.setModelOperationState(state);
//        try {
//            task.savePendingModifications(result);
//        } catch (ObjectAlreadyExistsException ex) {
//            throw new SystemException(ex);
//        }
//
//        // todo continue with model operation
//
//        markTaskAsClosed(task, result);         // brutal hack
//
////        switch (state.getKindOfOperation()) {
////            case ADD: modelController.addObjectContinuing(task, result); break;
////            case MODIFY: modelController.modifyObjectContinuing(task, result); break;
////            case DELETE: modelController.deleteObjectContinuing(task, result); break;
////            default: throw new SystemException("Unknown kind of model operation: " + state.getKindOfOperation());
////        }
//
//    }

    public String getProcessId(Task task) {
        // let us request the current task status
        // todo make this property single-valued in schema to be able to use getRealValue
        PrismProperty idProp = task.getExtension(WfTaskUtil.WFPROCESSID_PROPERTY_NAME);
        Collection<String> values = null;
        if (idProp != null) {
            values = idProp.getRealValues(String.class);
        }
        if (values == null || values.isEmpty()) {
            return null;
        } else {
            return values.iterator().next();
        }
    }

//    void setModelOperationState(Task task, ModelContext context) throws IOException {
//        ModelOperationStateType state = new ModelOperationStateType();
//        state.setOperationData(SerializationUtil.toString(context));
//        task.setModelOperationState(state);
//    }

    public boolean hasModelContext(Task task) {
        PrismProperty modelContextProperty = task.getExtension(ModelOperationTaskHandler.MODEL_CONTEXT_PROPERTY);
        return modelContextProperty != null && modelContextProperty.getRealValue() != null;
    }

    public ModelContext retrieveModelContext(Task task, OperationResult result) throws SchemaException {
        PrismProperty modelContextProperty = task.getExtension(ModelOperationTaskHandler.MODEL_CONTEXT_PROPERTY);
        if (modelContextProperty == null || modelContextProperty.getRealValue() == null) {
            throw new SystemException("No model context information in task " + task);
        }
        Object value = modelContextProperty.getRealValue();
        if (value instanceof Element || value instanceof JAXBElement) {
            value = prismContext.getPrismJaxbProcessor().unmarshalObject(value, LensContextType.class);
        }
        if (!(value instanceof LensContextType)) {
            throw new SystemException("Model context information in task " + task + " is of wrong type: " + modelContextProperty.getRealValue().getClass());
        }
        return LensContext.fromJaxb((LensContextType) value, prismContext);
    }

    public void storeModelContext(Task task, ModelContext context) throws SchemaException {
        Validate.notNull(context, "model context cannot be null");
        PrismProperty modelContextProperty = wfModelContextPropertyDefinition.instantiate();
        modelContextProperty.setRealValue(((LensContext) context).toJaxb());
        task.setExtensionProperty(modelContextProperty);
    }

    public void storeDeltasToProcess(List<ObjectDelta<Objectable>> deltas, Task task) throws SchemaException {

        PrismProperty<ObjectDeltaType> deltaToProcess = wfDeltaToProcessPropertyDefinition.instantiate();
        for (ObjectDelta<Objectable> delta : deltas) {
            deltaToProcess.addRealValue(DeltaConvertor.toObjectDeltaType(delta));
        }
        task.addExtensionProperty(deltaToProcess);
    }

    public void storeResultingDeltas(List<ObjectDelta<Objectable>> deltas, Task task) throws SchemaException {

        PrismProperty<ObjectDeltaType> resultingDeltas = wfResultingDeltaPropertyDefinition.instantiate();
        for (ObjectDelta delta : deltas) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Storing delta into task {}; delta = {}", task, delta.debugDump(0));
            }
            resultingDeltas.addRealValue(DeltaConvertor.toObjectDeltaType(delta));
        }
        task.addExtensionProperty(resultingDeltas);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Stored {} deltas into task {}", resultingDeltas.size(), task);
        }
    }


    public List<ObjectDelta<Objectable>> retrieveDeltasToProcess(Task task) throws SchemaException {

        PrismProperty<ObjectDeltaType> deltaTypePrismProperty = task.getExtension(WFDELTA_TO_PROCESS_PROPERTY_NAME);
        if (deltaTypePrismProperty == null) {
            throw new SchemaException("No " + WFDELTA_TO_PROCESS_PROPERTY_NAME + " in task extension; task = " + task);
        }
        List<ObjectDelta<Objectable>> retval = new ArrayList<ObjectDelta<Objectable>>();
        for (ObjectDeltaType objectDeltaType : deltaTypePrismProperty.getRealValues()) {
            retval.add(DeltaConvertor.createObjectDelta(objectDeltaType, prismContext));
        }
        return retval;
    }

    // it is possible that resulting deltas are not in the task
    public List<ObjectDelta<Objectable>> retrieveResultingDeltas(Task task) throws SchemaException {

        List<ObjectDelta<Objectable>> retval = new ArrayList<ObjectDelta<Objectable>>();

        PrismProperty<ObjectDeltaType> deltaTypePrismProperty = task.getExtension(WFRESULTING_DELTA_PROPERTY_NAME);
        if (deltaTypePrismProperty != null) {
            for (ObjectDeltaType objectDeltaType : deltaTypePrismProperty.getRealValues()) {
                retval.add(DeltaConvertor.createObjectDelta(objectDeltaType, prismContext));
            }
        }
        return retval;
    }

    public void setTaskNameIfEmpty(Task t, PolyStringType taskName) {
        if (t.getName() == null || t.getName().toPolyString().isEmpty()) {
            t.setName(taskName);
        }
    }


    public void setWfLastVariables(Task task, String value) throws SchemaException {
        PrismProperty wfLastVariablesProperty = wfLastVariablesPropertyDefinition.instantiate();
        wfLastVariablesProperty.setValue(new PrismPropertyValue<String>(value));
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("WfLastVariable INDEXED = " + wfLastVariablesProperty.getDefinition().isIndexed());
        }
        task.setExtensionProperty(wfLastVariablesProperty);

    }

    public void addWfStatus(Task task, String value) throws SchemaException {
        PrismProperty wfStatusProperty = wfStatusPropertyDefinition.instantiate();
        PrismPropertyValue<String> newValue = new PrismPropertyValue<String>(value);
        wfStatusProperty.addValue(newValue);
        task.addExtensionProperty(wfStatusProperty);
    }

    private<T> T getExtensionValue(Class<T> clazz, Task task, QName propertyName) {
        PrismProperty<String> property = task.getExtension(propertyName);
        return property != null ? property.getRealValue(clazz) : null;
    }

    public void setProcessInstanceFinishedImmediate(Task task, Boolean value, OperationResult result) throws SchemaException, ObjectNotFoundException {
        setExtensionPropertyImmediate(task, wfProcessInstanceFinishedPropertyDefinition, value, result);
    }

    public boolean isProcessInstanceFinished(Task task) {
        Boolean value = getExtensionValue(Boolean.class, task, WFPROCESS_INSTANCE_FINISHED_PROPERTY_NAME);
        return value != null ? value : false;
    }

    public void setSkipModelContextProcessingProperty(Task task, boolean value, OperationResult result) throws SchemaException, ObjectNotFoundException {
        setExtensionProperty(task, wfSkipModelContextProcessingPropertyDefinition, value, result);
    }

}
