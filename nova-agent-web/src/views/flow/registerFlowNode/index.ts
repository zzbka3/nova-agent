import registerStart from './registerStart'
import registerEnd from './registerEnd'
import registerWorkflowAgent from './registerWorkflowAgent'

// Register all node types on the LogicFlow instance
export function registerAllNodes(lf: any) {
  registerStart(lf)
  registerEnd(lf)
  registerWorkflowAgent(lf)
  // Additional node type registrations will be added here:
  // registerConditional(lf)
  // registerIntention(lf)
  // registerKnowledge(lf)
  // registerLargeModel(lf)
  // registerApi(lf)
  // registerMessage(lf)
  // registerCode(lf)
  // registerTextProcessor(lf)
  // registerMemory(lf)
}

export {
  registerStart,
  registerEnd,
  registerWorkflowAgent,
}
