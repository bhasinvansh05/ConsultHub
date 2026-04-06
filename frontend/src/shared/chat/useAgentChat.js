import { useCallback, useEffect, useRef, useState } from 'react'
import { getToken as defaultGetToken } from '../lib/auth'
import { sendAgentChatMessage } from './agentChatSend'

const UNEXPECTED =
  'Something unexpected went wrong on your device. Please refresh the page and try again.'

/**
 * Session state + send pipeline for the ConsultHub assistant (HTTP API).
 * Persists only conversationId in a ref (server-side history); do not mirror Gemini contents client-side.
 *
 * @param {object} options
 * @param {Array<{ id: number, role: string, content: string }>} options.initialMessages
 * @param {() => string|null|undefined} [options.getToken]
 */
export function useAgentChat({ initialMessages, getToken: getTokenFn = defaultGetToken }) {
  const [messages, setMessages] = useState(initialMessages)
  const [isLoading, setIsLoading] = useState(false)

  const conversationIdRef = useRef(null)
  const initialMessagesRef = useRef(initialMessages)
  initialMessagesRef.current = initialMessages

  const abortRef = useRef(null)
  const sendGenerationRef = useRef(0)
  const mountedRef = useRef(true)

  useEffect(() => {
    mountedRef.current = true
    return () => {
      mountedRef.current = false
      abortRef.current?.abort()
    }
  }, [])

  /**
   * Bound to PromptInputBox `onSend` — second argument is file list (not sent to API).
   * @param {string} text
   */
  const sendMessage = useCallback(
    async (text) => {
      const trimmed = typeof text === 'string' ? text.trim() : ''
      if (!trimmed) return

      const myGen = ++sendGenerationRef.current
      abortRef.current?.abort()
      const controller = new AbortController()
      abortRef.current = controller

      const userMsg = { id: Date.now(), role: 'user', content: trimmed }
      setMessages((prev) => [...prev, userMsg])
      setIsLoading(true)

      try {
        const token = getTokenFn()
        const result = await sendAgentChatMessage({
          message: trimmed,
          conversationId: conversationIdRef.current,
          token,
          signal: controller.signal,
        })

        if (!mountedRef.current || myGen !== sendGenerationRef.current) return

        if (result.ok) {
          if (result.conversationId != null) {
            conversationIdRef.current = result.conversationId
          }
          setMessages((prev) => [...prev, { id: Date.now() + 1, role: 'assistant', content: result.reply }])
        } else {
          if (result.error === 'Request was cancelled.') return
          setMessages((prev) => [...prev, { id: Date.now() + 1, role: 'assistant', content: result.error }])
        }
      } catch {
        if (!mountedRef.current || myGen !== sendGenerationRef.current) return
        setMessages((prev) => [...prev, { id: Date.now() + 1, role: 'assistant', content: UNEXPECTED }])
      } finally {
        if (mountedRef.current && myGen === sendGenerationRef.current) {
          setIsLoading(false)
        }
      }
    },
    [getTokenFn]
  )

  const resetConversation = useCallback(() => {
    abortRef.current?.abort()
    sendGenerationRef.current += 1
    conversationIdRef.current = null
    setIsLoading(false)
    setMessages(initialMessagesRef.current)
  }, [])

  return {
    messages,
    isLoading,
    sendMessage,
    resetConversation,
  }
}
