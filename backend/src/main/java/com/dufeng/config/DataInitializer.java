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
        Category clothing = ensureCategory("服饰鞋包", 0L, 2);
        Category food = ensureCategory("食品生鲜", 0L, 3);

        ensureBrand("华为");
        ensureBrand("小米");

        Shop shop = shopMapper.selectList(new LambdaQueryWrapper<Shop>().last("LIMIT 1")).stream().findFirst().orElse(null);
        if (shop == null) {
            return;
        }
        ensureGoods(shop.getId(), electronics.getId(), "华为 Mate 60 Pro", "旗舰影像，麒麟芯片",
                new BigDecimal("6999.00"), List.of("曜金黑", "雅川青"), "HC-MATE60", "/img/goods-1.jpg");
        ensureGoods(shop.getId(), electronics.getId(), "小米15 Pro", "骁龙8至尊版，徕卡影像",
                new BigDecimal("5299.00"), List.of("黑色", "白色"), "XM-15PRO", "/img/goods-2.jpg");
        ensureGoods(shop.getId(), clothing.getId(), "纯棉基础T恤", "柔软亲肤，四季百搭",
                new BigDecimal("89.00"), List.of("白色", "黑色"), "TC-BASE", "/img/goods-3.jpg");
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

    private void ensureBrand(String name) {
        Long count = brandMapper.selectCount(new LambdaQueryWrapper<Brand>().eq(Brand::getName, name));
        if (count == 0) {
            Brand brand = new Brand();
            brand.setName(name);
            brandMapper.insert(brand);
        }
    }

    private void ensureGoods(Long shopId, Long categoryId, String title, String subtitle,
                             BigDecimal price, List<String> specs, String skuCode, String imagePath) {
        Long count = goodsMapper.selectCount(new LambdaQueryWrapper<Goods>().eq(Goods::getTitle, title));
        if (count > 0) {
            return;
        }
        Goods goods = new Goods();
        goods.setShopId(shopId);
        goods.setCategoryId(categoryId);
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
