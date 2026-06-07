# Demo Guide

This guide describes how to run and verify the AI Novel Screenplay MVP demo.

## Demo Goal

The demo should prove that a user can complete the main workflow:

1. Enter or upload novel text.
2. Generate chapters, characters, scenes, and YAML.
3. Edit and validate YAML.
4. Export the final `screenplay.yaml` file.

## Prerequisites

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm.cmd install
npm.cmd run dev
```

Open the Vite URL shown in the terminal, usually:

```text
http://localhost:5173
```

PowerShell may block `npm`; use `npm.cmd` in that case.

## Demo Steps

### 1. Load Novel Text

Use one of these methods:

- Click `示例` to load the built-in sample novel text.
- Upload a UTF-8 `.txt` file.
- Paste novel text into the text area.

Expected result:

- The title field is populated.
- The novel text area contains content.
- Uploaded files show file name and character count.

### 2. Generate Screenplay Draft

Click `一键生成`.

Expected result:

- Chapter count is greater than 0.
- Character count is greater than 0.
- Scene count is greater than 0.
- The YAML editor is populated.

### 3. Inspect Structure

Open the structure tabs:

- `章节`: chapter title and word count are visible.
- `角色`: character name, description, and ID are visible.
- `场景`: scene title, location, time, source chapter, character IDs, and summary are visible.

Expected result:

- Scenes reference known character IDs.
- Scenes show source chapter information.

### 4. Validate YAML

Click `校验`.

Expected result:

- The validation panel appears.
- The panel shows valid or invalid status.
- The panel shows character, relationship, and scene counts.
- If validation fails, errors are listed.
- If validation passes with warnings, warnings are listed.

### 5. Copy and Export

Click `复制` to copy YAML.

Click `导出` after validation passes.

Expected result:

- Copy action shows a success message.
- Export downloads `screenplay.yaml`.

### 6. Reset Demo

Click `重置`.

Expected result:

- Novel text, upload info, chapters, characters, scenes, YAML, and validation result are cleared.
- The workflow can be started again.

## Acceptance Checklist

- [ ] Backend starts successfully on port `8080`.
- [ ] Frontend starts successfully with Vite.
- [ ] `GET /api/health` returns backend running status.
- [ ] Sample text can be loaded.
- [ ] Full generation produces chapters, characters, scenes, and YAML.
- [ ] YAML contains `metadata`, `characters`, `relationships`, `scenes`, and `production`.
- [ ] Validation returns errors, warnings, and counts.
- [ ] Valid YAML can be exported as `screenplay.yaml`.
- [ ] Reset clears the workbench.
- [ ] `mvn test` passes in `backend`.
- [ ] `npm.cmd run build` passes in `frontend`.

## Common Issues

### Backend service unavailable

Start the backend:

```bash
cd backend
mvn spring-boot:run
```

### PowerShell blocks npm

Use `npm.cmd`:

```bash
npm.cmd install
npm.cmd run dev
```

### YAML cannot be exported

Run validation first. Export is enabled only after YAML validation passes.

### Validation has warnings

Warnings do not necessarily block validity. They are quality or completeness hints, such as missing optional traceable blocks.
