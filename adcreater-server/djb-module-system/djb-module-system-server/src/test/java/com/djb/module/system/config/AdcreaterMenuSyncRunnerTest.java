package com.djb.module.system.config;

import com.djb.framework.tenant.core.context.TenantContextHolder;
import com.djb.framework.test.core.ut.BaseMockitoUnitTest;
import com.djb.module.system.dal.dataobject.permission.MenuDO;
import com.djb.module.system.dal.mysql.permission.MenuMapper;
import com.djb.module.system.dal.mysql.permission.RoleMenuMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdcreaterMenuSyncRunnerTest extends BaseMockitoUnitTest {

    @Mock
    private MenuMapper menuMapper;
    @Mock
    private RoleMenuMapper roleMenuMapper;

    @InjectMocks
    private AdcreaterMenuSyncRunner runner;

    @Test
    void run_shouldIgnoreTenantWhenCleaningLegacyMenus() {
        TenantContextHolder.clear();
        MenuDO legacyMenu = new MenuDO();
        legacyMenu.setId(1L);
        legacyMenu.setName("AI 旧菜单");
        legacyMenu.setPath("/ai/legacy");
        when(menuMapper.selectList()).thenReturn(List.of(legacyMenu));
        doAnswer(invocation -> {
            assertTrue(TenantContextHolder.isIgnore());
            return null;
        }).when(roleMenuMapper).deleteListByMenuId(legacyMenu.getId());
        doAnswer(invocation -> {
            assertTrue(TenantContextHolder.isIgnore());
            return 1;
        }).when(menuMapper).deleteById(legacyMenu.getId());
        doAnswer(invocation -> {
            assertTrue(TenantContextHolder.isIgnore());
            return 1;
        }).when(menuMapper).insert(any(MenuDO.class));

        runner.run(null);

        verify(roleMenuMapper).deleteListByMenuId(legacyMenu.getId());
        verify(menuMapper).deleteById(legacyMenu.getId());
        verify(menuMapper, atLeastOnce()).insert(any(MenuDO.class));
        assertFalse(TenantContextHolder.isIgnore());
    }
}
