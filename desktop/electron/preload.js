import { contextBridge, ipcRenderer } from 'electron'

contextBridge.exposeInMainWorld('electronAPI', {
  saveFile: (opts) => ipcRenderer.invoke('fs:save-file', opts),
  listFiles: (opts) => ipcRenderer.invoke('fs:list-files', opts),
  deleteFile: (filePath) => ipcRenderer.invoke('fs:delete-file', filePath),
  getTemplateDir: () => ipcRenderer.invoke('fs:get-template-dir'),
  getAssetsDir: () => ipcRenderer.invoke('fs:get-assets-dir')
})
