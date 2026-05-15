// APP 链接分组
export interface AppLinkGroup {
  // 分组名称
  name: string
  // 链接列表
  links: AppLink[]
}

// APP 链接
export interface AppLink {
  // 链接名称
  name: string
  // 链接地址
  path: string
  // 链接的类型
  type?: APP_LINK_TYPE_ENUM
}

// APP 链接类型（需要特殊处理，例如商品详情）
export const enum APP_LINK_TYPE_ENUM {
  // 拼团活动
  ACTIVITY_COMBINATION,
  // 秒杀活动
  ACTIVITY_SECKILL,
  // 积分商城活动
  ACTIVITY_POINT,
  // 文章详情
  ARTICLE_DETAIL,
  // 优惠券详情
  COUPON_DETAIL,
  // 自定义页面详情
  DIY_PAGE_DETAIL,
  // 品类列表
  PRODUCT_CATEGORY_LIST,
  // 商品列表
  PRODUCT_LIST,
  // 商品详情
  PRODUCT_DETAIL_NORMAL,
  // 拼团商品详情
  PRODUCT_DETAIL_COMBINATION,
  // 秒杀商品详情
  PRODUCT_DETAIL_SECKILL
}

// APP 链接列表（做一下持久化？）
export const APP_LINK_GROUP_LIST = [
  {
    name: '支付',
    links: [
      {
        name: '充值余额',
        path: '/pages/pay/recharge'
      },
      {
        name: '充值记录',
        path: '/pages/pay/recharge-log'
      }
    ]
  },
  {
    name: '用户中心',
    links: [
      {
        name: '用户信息',
        path: '/pages/user/info'
      },
      {
        name: '用户订单',
        path: '/pages/order/list'
      },
      {
        name: '售后订单',
        path: '/pages/order/aftersale/list'
      },
      {
        name: '商品收藏',
        path: '/pages/user/goods-collect'
      },
      {
        name: '浏览记录',
        path: '/pages/user/goods-log'
      },
      {
        name: '地址管理',
        path: '/pages/user/address/list'
      },
      {
        name: '用户佣金',
        path: '/pages/user/wallet/commission'
      },
      {
        name: '用户余额',
        path: '/pages/user/wallet/money'
      },
      {
        name: '用户积分',
        path: '/pages/user/wallet/score'
      }
    ]
  }
] as AppLinkGroup[]
