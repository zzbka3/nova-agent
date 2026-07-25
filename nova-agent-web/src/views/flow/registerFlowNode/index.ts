import registerStart from './registerStart'
import registerEnd from './registerEnd'
import registerWorkflowAgent from './registerWorkflowAgent'
import registerApi from './registerApi'
import registerIf from './registerIf'
import registerCode from './registerCode'
import registerKnowledge from './registerKnowledge'
import registerLlm from './registerLlm'
import registerTextProcessor from './registerTextProcessor'
import registerMemory from './registerMemory'
import registerWorkflow from './registerWorkflow'
import registerMessage from './registerMessage'
import registerIntent from './registerIntent'

export function registerAllNodes(lf: any) {
  registerStart(lf)
  registerEnd(lf)
  registerWorkflowAgent(lf)
  registerApi(lf)
  registerIf(lf)
  registerCode(lf)
  registerKnowledge(lf)
  registerLlm(lf)
  registerTextProcessor(lf)
  registerMemory(lf)
  registerWorkflow(lf)
  registerMessage(lf)
  registerIntent(lf)
}

export {
  registerStart, registerEnd, registerWorkflowAgent, registerApi,
  registerIf, registerCode, registerKnowledge, registerLlm,
  registerTextProcessor, registerMemory, registerWorkflow,
  registerMessage, registerIntent,
}
