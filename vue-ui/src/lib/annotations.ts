import type { DrawShape } from 'chessground/draw'

/**
 * A per-move annotation: free-text comment plus board shapes (arrows/circles).
 * Persisted inside PGN move comments in the Lichess-compatible format, e.g.
 *   { A great move [%cal Ge2e4,Rf1c4] [%csl Yd5] }
 */
export interface Annotation {
  comment: string
  shapes: DrawShape[]
}

/** chessground brush name -> single-letter code used in PGN [%cal]/[%csl] tokens. */
const BRUSH_TO_LETTER: Record<string, string> = {
  green: 'G',
  red: 'R',
  blue: 'B',
  yellow: 'Y',
  paleGreen: 'G',
  paleRed: 'R',
  paleBlue: 'B',
  paleGrey: 'B',
}

/** Inverse of BRUSH_TO_LETTER; the four colours Lichess emits. */
const LETTER_TO_BRUSH: Record<string, string> = {
  G: 'green',
  R: 'red',
  B: 'blue',
  Y: 'yellow',
}

function letterForBrush(brush: string | undefined): string {
  return (brush && BRUSH_TO_LETTER[brush]) || 'G'
}

/** A square key like "e4" (two chars: file + rank). */
function isSquare(value: string): boolean {
  return /^[a-h][1-8]$/.test(value)
}

/**
 * Serialize an annotation into a PGN comment body. Returns an empty string when
 * there is nothing to store (so the caller can skip setComment entirely).
 */
export function serializeAnnotation(annotation: Annotation | undefined): string {
  if (!annotation) return ''
  const arrows: string[] = []
  const circles: string[] = []
  for (const shape of annotation.shapes) {
    if (!shape.orig) continue
    if (shape.dest) {
      arrows.push(`${letterForBrush(shape.brush)}${shape.orig}${shape.dest}`)
    } else {
      circles.push(`${letterForBrush(shape.brush)}${shape.orig}`)
    }
  }
  const parts: string[] = []
  const text = annotation.comment.trim()
  if (text) parts.push(text)
  if (circles.length) parts.push(`[%csl ${circles.join(',')}]`)
  if (arrows.length) parts.push(`[%cal ${arrows.join(',')}]`)
  return parts.join(' ')
}

function parseShapeTokens(body: string, tag: string, expectDest: boolean): DrawShape[] {
  const shapes: DrawShape[] = []
  const re = new RegExp(`\\[%${tag}\\s+([^\\]]*)\\]`, 'g')
  let match: RegExpExecArray | null
  while ((match = re.exec(body)) !== null) {
    for (const raw of (match[1] ?? '').split(',')) {
      const token = raw.trim()
      if (!token) continue
      const brush = LETTER_TO_BRUSH[token[0] ?? ''] ?? 'green'
      const orig = token.slice(1, 3)
      if (!isSquare(orig)) continue
      if (expectDest) {
        const dest = token.slice(3, 5)
        if (!isSquare(dest)) continue
        shapes.push({ orig, dest, brush } as DrawShape)
      } else {
        shapes.push({ orig, brush } as DrawShape)
      }
    }
  }
  return shapes
}

/**
 * Parse a PGN comment body back into an annotation, splitting out [%cal]/[%csl]
 * shapes and leaving the remaining free text as the comment.
 */
export function parseAnnotation(body: string): Annotation {
  const shapes: DrawShape[] = [
    ...parseShapeTokens(body, 'csl', false),
    ...parseShapeTokens(body, 'cal', true),
  ]
  const comment = body
    .replace(/\[%(?:cal|csl)\s+[^\]]*\]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
  return { comment, shapes }
}

/** True when an annotation carries nothing worth persisting or displaying. */
export function isEmptyAnnotation(annotation: Annotation | undefined): boolean {
  return !annotation || (!annotation.comment.trim() && annotation.shapes.length === 0)
}
