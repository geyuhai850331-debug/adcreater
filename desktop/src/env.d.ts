/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

interface FileEntry {
  name: string
  path: string
  size: number
  mtime: string
}

interface SaveFileOptions {
  subDir: string
  filename: string
  dataUrl: string
}

interface ListFilesOptions {
  subDir: string
  month: string
}

interface ElectronAPI {
  saveFile: (opts: SaveFileOptions) => Promise<string>
  listFiles: (opts: ListFilesOptions) => Promise<FileEntry[]>
  deleteFile: (filePath: string) => Promise<void>
  getTemplateDir: () => Promise<string>
  getAssetsDir: () => Promise<string>
}

interface Window {
  electronAPI?: ElectronAPI
}
