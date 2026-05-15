import type { RouteMeta } from 'vue-router'
import { Icon } from '@/components/Icon'
import { useI18n } from '@/hooks/web/useI18n'
import { useLocaleStoreWithOut } from '@/store/modules/locale'

export const useRenderMenuTitle = () => {
  const renderMenuTitle = (meta: RouteMeta) => {
    const { t } = useI18n()
    const localeStore = useLocaleStoreWithOut()
    const currentLang = localeStore.getCurrentLocale.lang
    const { title = 'Please set title', enName, icon } = meta

    // 根据当前语言选择显示名称：英文时显示 enName，否则显示中文（通过 i18n）
    const displayTitle = currentLang === 'en' && enName ? enName : t(title as string)

    return icon ? (
      <>
        <Icon icon={meta.icon}></Icon>
        <span class="v-menu__title overflow-hidden overflow-ellipsis whitespace-nowrap">
          {displayTitle}
        </span>
      </>
    ) : (
      <span class="v-menu__title overflow-hidden overflow-ellipsis whitespace-nowrap">
        {displayTitle}
      </span>
    )
  }

  return {
    renderMenuTitle
  }
}
