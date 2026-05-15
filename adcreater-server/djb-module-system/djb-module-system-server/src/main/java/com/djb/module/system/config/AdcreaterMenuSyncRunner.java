package com.djb.module.system.config;

import com.djb.framework.common.enums.CommonStatusEnum;
import com.djb.framework.tenant.core.util.TenantUtils;
import com.djb.module.system.dal.dataobject.permission.MenuDO;
import com.djb.module.system.dal.mysql.permission.MenuMapper;
import com.djb.module.system.dal.mysql.permission.RoleMenuMapper;
import com.djb.module.system.enums.permission.MenuTypeEnum;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 同步 AdCreater 管理菜单，确保它出现在系统菜单管理中。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AdcreaterMenuSyncRunner implements ApplicationRunner {

    private static final String ROOT_NAME = "AdCreater 管理";
    private static final String ROOT_PATH = "/adcreater";

    @Resource
    private final MenuMapper menuMapper;
    @Resource
    private final RoleMenuMapper roleMenuMapper;

    @Override
    public void run(ApplicationArguments args) {
        TenantUtils.executeIgnore(() -> {
            cleanupLegacyAiMenus();
            MenuDO root = upsertRootMenu();
            upsertChildMenu(root.getId(), "仪表盘", "dashboard", "ep:data-analysis",
                    "adcreater/dashboard/index", "AdCreaterDashboard", null, 1);
            upsertChildMenu(root.getId(), "用户管理", "users", "ep:user",
                    "adcreater/user/index", "AdCreaterUser", "system:user:query", 2);
            upsertChildMenu(root.getId(), "点数管理", "billing", "ep:wallet",
                    "adcreater/billing/index", "AdCreaterBilling", "billing:query", 3);
            upsertChildMenu(root.getId(), "模型配置", "models", "ep:cpu",
                    "adcreater/model/index", "AdCreaterModel", "ai:model-config:query", 4);
            upsertChildMenu(root.getId(), "Prompt 配置", "prompts", "ep:edit-pen",
                    "adcreater/prompt/index", "AdCreaterPrompt", "ai:prompt:query", 5);
            upsertChildMenu(root.getId(), "模板管理", "templates", "ep:files",
                    "adcreater/template/index", "AdCreaterTemplate", "template:query", 6);
        });
    }

    private void cleanupLegacyAiMenus() {
        List<MenuDO> menus = menuMapper.selectList().stream()
                .filter(this::isLegacyAiMenu)
                .toList();
        for (MenuDO menu : menus) {
            roleMenuMapper.deleteListByMenuId(menu.getId());
            menuMapper.deleteById(menu.getId());
            log.info("[run][delete legacy AI menu({}) success]", menu.getName());
        }
    }

    private MenuDO upsertRootMenu() {
        MenuDO menu = findMenuByParentAndName(MenuDO.ID_ROOT, ROOT_NAME);
        if (menu == null) {
            menu = new MenuDO();
            menu.setParentId(MenuDO.ID_ROOT);
            menu.setName(ROOT_NAME);
        }
        menu.setEnName("AdCreater");
        menu.setPermission("");
        menu.setType(MenuTypeEnum.DIR.getType());
        menu.setSort(90);
        menu.setPath(ROOT_PATH);
        menu.setIcon("ep:setting");
        menu.setComponent("");
        menu.setComponentName("AdCreater");
        menu.setStatus(CommonStatusEnum.ENABLE.getStatus());
        menu.setVisible(Boolean.TRUE);
        menu.setKeepAlive(Boolean.FALSE);
        menu.setAlwaysShow(Boolean.TRUE);
        saveMenu(menu);
        return menu;
    }

    private void upsertChildMenu(Long parentId, String name, String path, String icon,
                                 String component, String componentName, String permission, int sort) {
        MenuDO menu = findMenuByParentAndName(parentId, name);
        if (menu == null) {
            menu = new MenuDO();
            menu.setParentId(parentId);
            menu.setName(name);
        }
        menu.setEnName(componentName);
        menu.setPermission(permission == null ? "" : permission);
        menu.setType(MenuTypeEnum.MENU.getType());
        menu.setSort(sort);
        menu.setPath(path);
        menu.setIcon(icon);
        menu.setComponent(component);
        menu.setComponentName(componentName);
        menu.setStatus(CommonStatusEnum.ENABLE.getStatus());
        menu.setVisible(Boolean.TRUE);
        menu.setKeepAlive(Boolean.FALSE);
        menu.setAlwaysShow(Boolean.FALSE);
        saveMenu(menu);
    }

    private MenuDO findMenuByParentAndName(Long parentId, String name) {
        return menuMapper.selectList().stream()
                .filter(item -> Objects.equals(item.getParentId(), parentId) && Objects.equals(item.getName(), name))
                .findFirst()
                .orElse(null);
    }

    private boolean isLegacyAiMenu(MenuDO menu) {
        String path = menu.getPath();
        String component = menu.getComponent();
        String componentName = menu.getComponentName();
        return startsWith(path, "/ai")
                || startsWith(component, "ai/")
                || startsWith(componentName, "Ai");
    }

    private boolean startsWith(String value, String prefix) {
        return value != null && value.startsWith(prefix);
    }

    private void saveMenu(MenuDO menu) {
        if (menu.getId() == null) {
            menuMapper.insert(menu);
            log.info("[run][insert AdCreater menu({}) success]", menu.getName());
            return;
        }
        menuMapper.updateById(menu);
        log.info("[run][update AdCreater menu({}) success]", menu.getName());
    }
}
