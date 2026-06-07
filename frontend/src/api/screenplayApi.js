const jsonHeaders = {
  'Content-Type': 'application/json'
}

async function parsePayload(response) {
  const text = await response.text()

  if (!text) {
    return null
  }

  try {
    return JSON.parse(text)
  } catch {
    throw new Error(response.ok ? '响应格式错误' : `服务返回异常 (${response.status})`)
  }
}

async function request(path, options) {
  let response

  try {
    response = await fetch(path, options)
  } catch {
    throw new Error('后端服务不可用，请确认服务已启动')
  }

  const payload = await parsePayload(response)

  if (!response.ok || payload?.code !== 0) {
    throw new Error(payload?.message || `请求失败 (${response.status})`)
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
