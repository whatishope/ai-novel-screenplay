const jsonHeaders = {
  'Content-Type': 'application/json'
}

async function request(path, options) {
  const response = await fetch(path, options)
  const payload = await response.json()

  if (!response.ok || payload.code !== 0) {
    throw new Error(payload.message || '请求失败')
  }

  return payload.data
}

export function uploadNovel(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request('/api/novel/upload', {
    method: 'POST',
    body: formData
  })
}

export function splitChapters(text) {
  return request('/api/novel/split-chapters', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ text })
  })
}

export function extractCharacters(chapters) {
  return request('/api/screenplay/extract-characters', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ chapters })
  })
}

export function planScenes(chapters, characters) {
  return request('/api/screenplay/plan-scenes', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ chapters, characters })
  })
}

export function generateYaml(title, chapters, characters, scenes) {
  return request('/api/screenplay/generate-yaml', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ title, chapters, characters, scenes })
  })
}

export function generateFromText(title, text) {
  return request('/api/screenplay/generate-from-text', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ title, text })
  })
}

export function validateYaml(yamlText) {
  return request('/api/screenplay/validate-yaml', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ yamlText })
  })
}
