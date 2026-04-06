/**
 * Maps HTTP status + parsed JSON to a single user-visible outcome (success reply or error string).
 * Aligns with AgentChatResponse and Spring error bodies.
 */

export const MSG_NO_REPLY = 'No response was returned. Please try again.'

export const MSG_NETWORK =
  "We couldn't reach the assistant. Check your connection and that the server is running, then try again."

export const MSG_BAD_RESPONSE =
  'The server returned an unexpected response. Please try again or contact support if this continues.'

function joinValidationDetails(data) {
  const errors = data?.errors
  if (!Array.isArray(errors) || errors.length === 0) return null
  const parts = errors.map((e) => {
    if (typeof e === 'string') return e
    if (e && typeof e === 'object') {
      const f = e.field ?? e.fieldName
      const m = e.message ?? e.defaultMessage
      if (f && m) return `${f}: ${m}`
      return m || f || JSON.stringify(e)
    }
    return String(e)
  })
  return parts.join(' ')
}

/**
 * @param {object|null} data
 * @param {number} status
 */
export function messageFromErrorBody(data, status) {
  if (!data || typeof data !== 'object') {
    if (status === 401) return 'Please sign in to use the assistant.'
    if (status === 403) return "You don't have permission to use the assistant for this action."
    if (status === 404) return 'The chat service was not found. Please try again later.'
    if (status === 429) return 'Too many requests. Please wait a moment and try again.'
    if (status >= 500) return 'The assistant is temporarily unavailable. Please try again later.'
    return 'Something went wrong. Please try again.'
  }

  if (typeof data.reply === 'string' && data.reply.trim()) {
    return data.reply.trim()
  }

  if (typeof data.message === 'string' && data.message.trim()) {
    return data.message.trim()
  }

  if (typeof data.error === 'string' && data.error.trim()) {
    return data.error.trim()
  }

  const validation = joinValidationDetails(data)
  if (validation) return validation

  if (data.details && typeof data.details === 'object') {
    const entries = Object.entries(data.details)
    if (entries.length > 0) {
      return entries.map(([k, v]) => `${k}: ${v}`).join(' ')
    }
  }

  if (status === 401) return 'Please sign in to use the assistant.'
  if (status === 403) return "You don't have permission to complete this request."
  if (status === 404) return 'The requested resource was not found.'
  if (status === 429) return 'Too many requests. Please wait a moment and try again.'
  if (status >= 500) return 'The assistant is temporarily unavailable. Please try again later.'

  return 'Something went wrong. Please try again.'
}

/**
 * @param {Response} res
 * @param {object|null} data
 * @returns {{ ok: true, reply: string, conversationId?: number|null, conversationHistory?: unknown } | { ok: false, error: string }}
 */
export function normalizeAgentChatHttpExchange(res, data) {
  const status = res.status

  const successPayload =
    data &&
    typeof data === 'object' &&
    typeof data.reply === 'string' &&
    data.reply.trim()

  if (res.ok && successPayload) {
    return {
      ok: true,
      reply: data.reply.trim(),
      conversationId: data.conversationId ?? undefined,
      conversationHistory: data.conversationHistory,
    }
  }

  if (res.ok) {
    if (!data || typeof data !== 'object') {
      return {
        ok: false,
        error:
          'The assistant returned an empty or invalid response. If you are not using the dev server, ensure API requests are proxied to the backend.',
      }
    }
    return { ok: false, error: MSG_NO_REPLY }
  }

  if (status === 500 && successPayload) {
    return {
      ok: true,
      reply: data.reply.trim(),
      conversationId: data.conversationId ?? undefined,
      conversationHistory: data.conversationHistory,
    }
  }

  if (!data) {
    if (status === 502 || status === 503 || status === 504) {
      return {
        ok: false,
        error:
          'The assistant service is temporarily unreachable (gateway or timeout). Please try again in a moment.',
      }
    }
    return { ok: false, error: messageFromErrorBody(null, status) }
  }

  return { ok: false, error: messageFromErrorBody(data, status) }
}
