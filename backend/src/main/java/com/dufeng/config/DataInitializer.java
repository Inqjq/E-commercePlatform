package com.dufeng.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dufeng.common.constant.SecurityConstants;
import com.dufeng.module.admin.entity.Role;
import com.dufeng.module.admin.mapper.RoleMapper;
import com.dufeng.module.goods.entity.Brand;
import com.dufeng.module.goods.entity.Category;
import com.dufeng.module.goods.entity.Goods;
import com.dufeng.module.goods.entity.Sku;
import com.dufeng.module.goods.mapper.BrandMapper;
import com.dufeng.module.goods.mapper.CategoryMapper;
import com.dufeng.module.goods.mapper.GoodsMapper;
import com.dufeng.module.goods.mapper.SkuMapper;
import com.dufeng.module.merchant.entity.Merchant;
import com.dufeng.module.merchant.entity.Shop;
import com.dufeng.module.merchant.mapper.MerchantMapper;
import com.dufeng.module.merchant.mapper.ShopMapper;
import com.dufeng.module.user.entity.User;
import com.dufeng.module.user.entity.UserRole;
import com.dufeng.module.user.mapper.UserMapper;
import com.dufeng.module.user.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 启动时初始化演示数据，便于本地联调。均为幂等操作。
 * 仅在 dev 环境生效，避免弱口令演示账号泄漏到生产。
 */
@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final MerchantMapper merchantMapper;
    private final ShopMapper shopMapper;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final GoodsMapper goodsMapper;
    private final SkuMapper skuMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            seedAccounts();
            seedCatalog();
            log.info("[DataInitializer] 演示数据初始化完成。管理员: admin/admin123，商家: merchant/merchant123，用户: demo/demo123");
        } catch (Exception e) {
            log.warn("[DataInitializer] 初始化演示数据失败：{}", e.getMessage());
        }
    }

    private void seedAccounts() {
        Long adminId = ensureUser("admin", "admin123", "平台管理员", SecurityConstants.ROLE_ADMIN);
        Long merchantAccountId = ensureUser("merchant", "merchant123", "演示商家", SecurityConstants.ROLE_MERCHANT);
        ensureUser("demo", "demo123", "演示用户", SecurityConstants.ROLE_USER);

        if (adminId != null) {
            ensureRole(adminId, SecurityConstants.ROLE_ADMIN);
        }
        if (merchantAccountId != null) {
            ensureRole(merchantAccountId, SecurityConstants.ROLE_MERCHANT);
            Merchant merchant = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                    .eq(Merchant::getAccountId, merchantAccountId));
            if (merchant == null) {
                merchant = new Merchant();
                merchant.setAccountId(merchantAccountId);
                merchant.setName("演示商家旗舰店");
                merchant.setLicenseNo("91110000MA00000000");
                merchant.setLegalPerson("张三");
                merchant.setContactPhone("13800000000");
                merchant.setAuditStatus(1);
                merchant.setStatus(1);
                merchantMapper.insert(merchant);
                Shop shop = new Shop();
                shop.setMerchantId(merchant.getId());
                shop.setName("演示旗舰店");
                shop.setIntro("专注品质好物");
                shop.setServicePhone("400-000-0000");
                shop.setStatus(1);
                shopMapper.insert(shop);
            }
        }
    }

    private void seedCatalog() {
        Category electronics = ensureCategory("数码电子", 0L, 1);
        Category appliance = ensureCategory("家用电器", 0L, 2);
        Category clothing = ensureCategory("服饰鞋包", 0L, 3);
        Category beauty = ensureCategory("美妆护肤", 0L, 4);
        Category food = ensureCategory("食品生鲜", 0L, 5);

        // 二级分类
        Category phone = ensureCategory("手机", electronics.getId(), 1);
        Category laptop = ensureCategory("笔记本", electronics.getId(), 2);
        Category tablet = ensureCategory("平板", electronics.getId(), 3);
        Category tv = ensureCategory("电视", appliance.getId(), 1);
        Category fridge = ensureCategory("冰箱", appliance.getId(), 2);
        Category washer = ensureCategory("洗衣机", appliance.getId(), 3);
        Category menswear = ensureCategory("男装", clothing.getId(), 1);
        Category womenswear = ensureCategory("女装", clothing.getId(), 2);
        Category shoes = ensureCategory("鞋靴", clothing.getId(), 3);
        Category skincare = ensureCategory("面部护肤", beauty.getId(), 1);
        Category makeup = ensureCategory("彩妆", beauty.getId(), 2);
        Category snacks = ensureCategory("休闲零食", food.getId(), 1);
        Category fruits = ensureCategory("水果", food.getId(), 2);

        Long huawei = ensureBrand("华为");
        Long xiaomi = ensureBrand("小米");
        Long apple = ensureBrand("苹果");
        Long midea = ensureBrand("美的");
        Long haier = ensureBrand("海尔");
        Long uniqlo = ensureBrand("优衣库");
        Long belle = ensureBrand("百丽");
        Long origins = ensureBrand("悦木之源");
        Long perfectDiary = ensureBrand("完美日记");
        Long threeSquirrels = ensureBrand("三只松鼠");

        Shop shop = shopMapper.selectList(new LambdaQueryWrapper<Shop>().last("LIMIT 1")).stream().findFirst().orElse(null);
        if (shop == null) {
            return;
        }
        ensureGoods(shop.getId(), phone.getId(), huawei, "华为 Mate 60 Pro", "旗舰影像，麒麟芯片",
                new BigDecimal("6999.00"), List.of("曜金黑", "雅川青"), "HC-MATE60", "/img/goods-1.jpg");
        ensureGoods(shop.getId(), phone.getId(), xiaomi, "小米15 Pro", "骁龙8至尊版，徕卡影像",
                new BigDecimal("5299.00"), List.of("黑色", "白色"), "XM-15PRO", "/img/goods-2.jpg");
        ensureGoods(shop.getId(), phone.getId(), apple, "Apple iPhone 15 Pro", "A17 Pro 芯片，钛金属机身",
                new BigDecimal("7999.00"), List.of("原色钛金属", "蓝色钛金属"), "AP-15PRO", "/img/goods-4.jpg");
        ensureGoods(shop.getId(), laptop.getId(), apple, "Apple MacBook Air M3", "M3 芯片，轻薄长续航",
                new BigDecimal("9499.00"), List.of("午夜色", "星光色"), "AP-MBA-M3", "/img/goods-6.jpg");
        ensureGoods(shop.getId(), tablet.getId(), xiaomi, "小米平板 6 Pro", "2.8K 高刷屏，骁龙8+",
                new BigDecimal("2499.00"), List.of("黑色", "蓝色"), "XM-PAD6P", "/img/goods-5.jpg");
        ensureGoods(shop.getId(), tv.getId(), midea, "美的 55英寸 4K 智能电视", "4K 超高清，全面屏",
                new BigDecimal("2999.00"), List.of("55英寸"), "MD-TV55", "/img/goods-7.jpg");
        ensureGoods(shop.getId(), fridge.getId(), haier, "海尔 505L 十字对开门冰箱", "一级能效，风冷无霜",
                new BigDecimal("4599.00"), List.of("星空灰"), "HAIER-505", "/img/goods-8.jpg");
        ensureGoods(shop.getId(), menswear.getId(), uniqlo, "优衣库 男装 防风连帽外套", "防风防泼水，轻盈保暖",
                new BigDecimal("399.00"), List.of("深灰", "藏青"), "UNI-JACKET", "/img/goods-9.jpg");
        ensureGoods(shop.getId(), womenswear.getId(), uniqlo, "女装 法式碎花连衣裙", "收腰显瘦，度假风",
                new BigDecimal("259.00"), List.of("碎花"), "DRESS-FL", "/img/goods-10.jpg");
        ensureGoods(shop.getId(), shoes.getId(), belle, "百丽 女士平底单鞋", "软底舒适，通勤百搭",
                new BigDecimal("329.00"), List.of("黑色", "米白"), "BELLE-SHOE", "/img/goods-11.jpg");
        ensureGoods(shop.getId(), skincare.getId(), origins, "悦木之源 灵芝焕能精华水", "修护保湿，舒缓维稳",
                new BigDecimal("580.00"), List.of("200ml"), "OM-ESSENCE", "/img/goods-12.jpg");
        ensureGoods(shop.getId(), makeup.getId(), perfectDiary, "完美日记 哑光唇釉", "雾面显白，持久不拔干",
                new BigDecimal("89.00"), List.of("豆沙色", "正红色"), "PERFECT-LIP", "/img/goods-13.jpg");
        ensureGoods(shop.getId(), snacks.getId(), threeSquirrels, "三只松鼠 每日坚果礼盒", "科学配比，新鲜锁鲜",
                new BigDecimal("129.00"), List.of("30包"), "SZS-NUTS", "/img/goods-14.jpg");
        ensureGoods(shop.getId(), fruits.getId(), null, "智利车厘子 2斤装", "JJ 级大果，甜脆多汁",
                new BigDecimal("99.00"), List.of("2斤装"), "CHERRY-JJ", "/img/goods-15.jpg");
    }

    private Long ensureUser(String username, String password, String nickname, String role) {
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (existing != null) {
            return existing.getId();
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setGender(0);
        user.setStatus(1);
        userMapper.insert(user);
        ensureRole(user.getId(), role);
        return user.getId();
    }

    private void ensureRole(Long userId, String code) {
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, code));
        if (role == null) {
            return;
        }
        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, role.getId()));
        if (count == 0) {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(role.getId());
            userRoleMapper.insert(ur);
        }
    }

    private Category ensureCategory(String name, Long parentId, int sort) {
        Category existing = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, name)
                .eq(Category::getParentId, parentId));
        if (existing != null) {
            return existing;
        }
        Category category = new Category();
        category.setParentId(parentId);
        category.setName(name);
        category.setLevel(parentId == 0L || parentId == null ? 1 : 2);
        category.setSort(sort);
        category.setStatus(1);
        categoryMapper.insert(category);
        return category;
    }

    private Long ensureBrand(String name) {
        Brand existing = brandMapper.selectOne(new LambdaQueryWrapper<Brand>().eq(Brand::getName, name));
        if (existing != null) {
            return existing.getId();
        }
        Brand brand = new Brand();
        brand.setName(name);
        brandMapper.insert(brand);
        return brand.getId();
    }

    private void ensureGoods(Long shopId, Long categoryId, Long brandId, String title, String subtitle,
                             BigDecimal price, List<String> specs, String skuCode, String imagePath) {
        Goods goods = goodsMapper.selectOne(new LambdaQueryWrapper<Goods>().eq(Goods::getTitle, title));
        if (goods != null) {
            // 演示数据重建分类/品牌后，回填已存在商品的归属，保持筛选口径一致
            if (!java.util.Objects.equals(goods.getCategoryId(), categoryId)
                    || !java.util.Objects.equals(goods.getBrandId(), brandId)) {
                Goods patch = new Goods();
                patch.setId(goods.getId());
                patch.setCategoryId(categoryId);
                patch.setBrandId(brandId);
                goodsMapper.updateById(patch);
            }
            return;
        }
        goods = new Goods();
        goods.setShopId(shopId);
        goods.setCategoryId(categoryId);
        goods.setBrandId(brandId);
        goods.setTitle(title);
        goods.setSubtitle(subtitle);
        goods.setMainImage(imagePath);
        goods.setImages("[{\"url\":\"" + imagePath + "\"}]");
        goods.setDetail("<p>" + subtitle + "</p>");
        goods.setPrice(price);
        goods.setStatus(2);
        goods.setAuditStatus(2);
        goods.setSales(0);
        goodsMapper.insert(goods);
        for (int i = 0; i < specs.size(); i++) {
            Sku sku = new Sku();
            sku.setGoodsId(goods.getId());
            sku.setSkuCode(skuCode + "-" + (i + 1));
            sku.setSpecText(specs.get(i));
            sku.setSpecJson("{\"款式\":\"" + specs.get(i) + "\"}");
            sku.setPrice(price);
            sku.setStock(100);
            sku.setStatus(1);
            skuMapper.insert(sku);
        }
    }
}
