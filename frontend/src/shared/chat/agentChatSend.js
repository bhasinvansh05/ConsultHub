import { postAgentChatRequest } from './agentChatHttp'
import { MSG_NETWORK, normalizeAgentChatHttpExchange } from './normalizeAgentChatResponse'

/**
 * Full round-trip: HTTP POST + normalize to success/error for the UI.
 * @param {object} params
 * @param {string} params.message
 * @param {number|null|undefined} params.conversationId
 * @param {string|null|undefined} params.token
 * @param {AbortSignal} [params.signal]
 */
export async function sendAgentChatMessage({ message, conversationId, token, signal }) {
  try {
    const { res, data } = await postAgentChatRequest({
      message,
      conversationId,
      token,
      signal,
    })
    return normalizeAgentChatHttpExchange(res, data)
  } catch (e) {
    if (e?.name === 'AbortError') {
      return { ok: false, error: 'Request was cancelled.' }
    }
    return { ok: false, error: MSG_NETWORK }
  }
}
