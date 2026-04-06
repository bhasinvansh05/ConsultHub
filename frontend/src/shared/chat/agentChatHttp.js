/**
 * HTTP transport for the agent assistant. The backend exposes POST /api/agent/chat only
 * (no WebSocket); this module owns the fetch lifecycle and raw body parsing.
 */

export const AGENT_CHAT_PATH = '/api/agent/chat'

/**
 * Read body as text and parse JSON. Returns null for empty or invalid JSON.
 * @param {Response} res
 * @returns {Promise<object|null>}
 */
export async function readResponseJson(res) {
  const text = await res.text()
  if (!text) return null
  const trimmed = text.replace(/^\uFEFF/, '').trim()
  if (!trimmed) return null
  try {
    return JSON.parse(trimmed)
  } catch {
    return null
  }
}

/**
 * Build JSON body for AgentChatRequest (matches backend DTO).
 * Do not send conversationHistory from the client: AgentChatService rebuilds turns from the DB
 * using conversationId; a large Gemini contents blob on every message can break proxies or limits.
 * @param {{ message: string, conversationId?: number|null }} params
 */
export function buildAgentChatRequestBody({ message, conversationId }) {
  return {
    message,
    ...(conversationId != null ? { conversationId } : {}),
  }
}

/**
 * POST /api/agent/chat. Does not throw on HTTP error status; throws only on network/abort.
 * @param {object} params
 * @param {string} params.message
 * @param {number|null|undefined} params.conversationId
 * @param {string|null|undefined} params.token
 * @param {AbortSignal} [params.signal]
 * @returns {Promise<{ res: Response, data: object|null }>}
 */
export async function postAgentChatRequest({ message, conversationId, token, signal }) {
  const body = buildAgentChatRequestBody({ message, conversationId })
  const res = await fetch(AGENT_CHAT_PATH, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(body),
    signal,
  })
  const data = await readResponseJson(res)
  return { res, data }
}
