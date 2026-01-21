# 《速写一日》日程管理APP

一个功能完整的Android日程管理应用，采用四象限时间管理法，支持多视图切换和智能提醒。

![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Android](https://img.shields.io/badge/Android-6.0+-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-8+-orange.svg)

## 📱 功能特性

### 核心功能

- ✅ **用户认证系统**
  - 手机号注册与登录
  - 密码加密存储（SHA-256 + 盐值）
  - 自动登录与状态保持

- ✅ **多视图展示**
  - **四象限视图**：按重要性和紧急性分类管理任务
  - **日视图**：以时间轴形式展示当天日程
  - **周视图**：以网格形式展示一周安排
  - **月视图**：以日历形式展示整月概览

- ✅ **日程管理**
  - 添加、编辑、删除日程
  - 支持重复日程（每日/每周/每月）
  - 日程颜色标记（10种颜色可选）
  - 四象限分类（重要且紧急/重要不紧急/紧急不重要/不重要不紧急）

- ✅ **智能提醒**
  - 自定义提醒时间（15分钟/30分钟/1小时/1天前）
  - 系统通知栏提醒
  - 支持开机自启，确保提醒不丢失
  - 通知操作（稍后提醒/关闭）

- ✅ **搜索与筛选**
  - 按标题和内容搜索
  - 多维度筛选（全部/未完成/已完成/已逾期）
  - 实时搜索结果更新

- ✅ **个人中心**
  - 个人信息管理（昵称、性别、年龄）
  - 我的日程（懒加载历史和未来数据）
  - 密码修改功能
  - 退出登录

- ✅ **数据管理**
  - 本地SQLite数据库存储
  - 数据持久化
  - 支持数据导出（可扩展）

## 🚀 快速开始

### 环境要求

- Android Studio 4.0+
- JDK 8+
- Android SDK 23+ (Android 6.0 Marshmallow)
- Gradle 8.10.2

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/chen-kangkang/CalendarApplication.git
cd CalendarApplication
```

2. **打开项目**
- 启动Android Studio
- 选择"Open an existing project"
- 选择项目目录

3. **同步项目**
- 等待Gradle自动同步完成
- 如果同步失败，点击"Sync Project with Gradle Files"

4. **运行项目**
- 连接Android设备（开启USB调试）或启动模拟器
- 点击工具栏的"Run"按钮（绿色三角形）
- 选择目标设备
- 等待应用安装并启动

### 直接安装

你也可以直接下载APK文件安装到设备：

1. 前往 [Releases](https://github.com/chen-kangkang/CalendarApplication/releases) 页面
2. 下载最新版本的APK文件
3. 在Android设备上允许"未知来源"安装
4. 点击APK文件进行安装

## 📁 项目结构

```
CalendarApplication/
├── app/                                    # 主应用模块
│   ├── src/main/
│   │   ├── java/com/example/calendarapplication/
│   │   │   ├── db/                        # 数据库层
│   │   │   │   ├── DatabaseHelper.java    # 数据库帮助类
│   │   │   │   ├── ScheduleDAO.java       # 日程数据访问对象
│   │   │   │   └── UserDAO.java           # 用户数据访问对象
│   │   │   ├── model/                     # 数据模型
│   │   │   │   ├── Schedule.java          # 日程实体类
│   │   │   │   └── User.java              # 用户实体类
│   │   │   ├── ui/                        # 界面层
│   │   │   │   ├── MainActivity.java      # 主界面（四象限视图）
│   │   │   │   ├── ViewActivity.java      # 视图页面（日/周/月视图）
│   │   │   │   ├── LoginActivity.java     # 登录界面
│   │   │   │   ├── RegisterActivity.java  # 注册界面
│   │   │   │   ├── AddScheduleActivity.java   # 添加日程
│   │   │   │   ├── EditScheduleActivity.java  # 编辑日程
│   │   │   │   ├── SearchActivity.java    # 搜索界面
│   │   │   │   ├── MyActivity.java        # 个人中心
│   │   │   │   ├── MyScheduleActivity.java    # 我的日程
│   │   │   │   ├── BasicInfoActivity.java     # 个人资料
│   │   │   │   ├── ChangePasswordActivity.java # 修改密码
│   │   │   │   └── ...
│   │   │   ├── utils/                     # 工具类
│   │   │   │   ├── AlarmTool.java         # 闹钟工具
│   │   │   │   ├── DateUtils.java         # 日期工具
│   │   │   │   ├── PasswordUtils.java     # 密码加密工具
│   │   │   │   └── ...
│   │   │   ├── receiver/                  # 广播接收器
│   │   │   │   └── BootCompletedReceiver.java # 开机自启
│   │   │   └── CalendarApplication.java   # 应用入口
│   │   └── res/                           # 资源文件
│   │       ├── layout/                    # 布局文件
│   │       ├── drawable/                  # 图片资源
│   │       ├── values/                    # 配置文件
│   │       └── ...
│   └── build.gradle                        # 模块构建配置
├── gradle/                                 # Gradle配置
├── build.gradle                            # 项目构建配置
├── gradle.properties                       # Gradle属性
├── gradlew                                 # Gradle包装器（Linux/Mac）
├── gradlew.bat                             # Gradle包装器（Windows）
├── settings.gradle                         # 项目设置
├── README.md                               # 项目说明
├── LICENSE                                 # MIT许可证
├── .gitignore                              # Git忽略配置
└── docs/                                   # GitHub Pages网站
    ├── index.html                          # 网站主页
    └── assets/                             # 网站资源
        └── images/
            └── screenshots/                # 应用截图
                ├── main_activity.png       # 主界面截图
                ├── day_view.png            # 日视图界面截图
                ├── week_view.png           # 周视图界面截图
                ├── month_view.png          # 月视图界面截图
                ├── login_activity.png      # 登录注册界面截图
                ├── add_schedule.png        # 添加日程界面截图
                ├── edit_schedule.png       # 编辑日程界面截图
                └── my_activity.png         # 个人中心界面截图

```

## 🛠️ 技术栈

### 开发环境

- **开发语言**：Java 8+
- **构建工具**：Gradle 8.10.2
- **Android Gradle插件**：8.4.0
- **目标SDK**：Android 14 (API 34)
- **最低SDK**：Android 6.0 (API 23)

### 核心技术

- **UI框架**：Android原生Views
- **数据存储**：SQLite + ContentValues
- **数据加密**：SHA-256 + 盐值
- **闹钟提醒**：AlarmManager + NotificationCompat
- **广播接收**：BroadcastReceiver
- **日期处理**：Calendar + SimpleDateFormat

### 第三方库

- **AndroidX**：Android Jetpack组件
- **Material Design**：Material Components
- **RecyclerView**：列表组件
- **ViewPager2**：页面切换

## 📖 使用指南

### 首次使用

1. **注册账号**
   - 打开应用
   - 点击"注册"按钮
   - 输入手机号（11位）
   - 设置密码（至少6位）
   - 确认密码并完成注册

2. **登录应用**
   - 输入注册的手机号和密码
   - 点击"登录"按钮
   - 进入主界面

### 日常使用

#### 四象限视图

1. 查看当天的日程分类
2. 点击右下角"+"添加新日程
3. 点击日程卡片进行编辑
4. 长按日程卡片进行删除
5. 点击顶部日期切换到其他日期

#### 日/周/月视图

1. 点击底部导航栏"视图"图标
2. 选择视图类型（日/周/月）
3. 左右滑动切换日期/周/月
4. 点击日程进行编辑
5. 点击"+"添加新日程

#### 搜索日程

1. 点击顶部搜索图标
2. 输入关键词
3. 选择筛选条件
4. 点击搜索结果查看详情

### 高级功能

#### 重复日程

1. 添加日程时选择"重复"选项
2. 选择重复类型（每日/每周/每月）
3. 设置重复结束日期
4. 系统自动为每个日期创建独立记录

#### 提醒设置

1. 添加或编辑日程时选择"提醒"选项
2. 选择提醒时间（15分钟/30分钟/1小时/1天前）
3. 系统会在指定时间发送通知

## 📊 数据库设计

### 用户表（user）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| _id | INTEGER | 主键，自增 |
| phone | TEXT | 手机号（唯一，非空） |
| password | TEXT | 加密后的密码（非空） |
| salt | TEXT | 盐值（非空） |
| nickname | TEXT | 昵称（默认："我是一个用户昵称"） |
| avatar | TEXT | 头像URL（默认：空字符串） |
| gender | TEXT | 性别（默认：空字符串） |
| age | INTEGER | 年龄（默认：0） |

### 日程表（schedule）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| _id | INTEGER | 主键，自增 |
| date | TEXT | 日期（格式：yyyy-MM-dd） |
| title | TEXT | 日程标题 |
| time | TEXT | 时间（格式：HH:mm-HH:mm） |
| completed | INTEGER | 完成状态（0未完成/1已完成） |
| quadrant | INTEGER | 四象限（0红/1黄/2绿/3蓝） |
| remark | TEXT | 备注内容 |
| color | INTEGER | 事项颜色（默认：0） |
| duration | INTEGER | 持续时间（默认：1） |
| reminder | TEXT | 提醒时间 |
| repeat_type | TEXT | 重复类型（默认：NONE） |
| user_id | INTEGER | 用户ID（外键，非空） |

### 四象限说明

| 数值 | 颜色 | 象限 | 说明 |
|------|------|------|------|
| 0 | 红色 | 第一象限 | 重要且紧急 |
| 1 | 黄色 | 第二象限 | 重要不紧急 |
| 2 | 绿色 | 第三象限 | 紧急不重要 |
| 3 | 蓝色 | 第四象限 | 不紧急不重要 |

## 🎨 设计理念

### 四象限时间管理法

应用基于史蒂芬·柯维的四象限时间管理法：

- **第一象限（重要且紧急）**：红色，需要立即处理
- **第二象限（重要不紧急）**：蓝色，需要重点关注
- **第三象限（紧急不重要）**：黄色，尽量授权他人
- **第四象限（不重要不紧急）**：绿色，尽量减少或避免

### 用户体验设计

- 简洁直观的界面设计
- 流畅的操作体验
- 多视图切换，满足不同场景需求
- 智能提醒，不错过重要事项

## 🐛 常见问题

### 1. 忘记密码怎么办？

当前版本不支持密码找回功能。建议：
- 使用手机号重新注册
- 或联系开发者协助处理

未来版本将添加密码找回功能。

### 2. 提醒不生效？

可能原因：
- 应用被强制停止
- 通知权限被禁用
- 设备重启后未自启

解决方法：
- 确保应用在后台运行
- 检查并开启通知权限
- 允许应用开机自启

### 3. 数据丢失怎么办？

数据存储在本地SQLite数据库中，卸载应用或清除数据会导致数据丢失。

建议：
- 定期备份数据（可通过导出功能）
- 不要随意卸载或清除应用数据

### 4. 日程在视图中不显示？

可能原因：
- 日期选择错误
- 筛选条件限制
- 数据未正确加载

解决方法：
- 确认选择的日期正确
- 检查筛选条件是否为"全部"
- 刷新页面重新加载数据

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🤝 贡献指南

欢迎贡献代码、提出建议或报告bug！

### 贡献方式

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范

- 遵循Android官方代码规范
- 使用有意义的变量和方法名
- 添加必要的注释
- 保持代码简洁清晰

## 📞 联系方式

如有问题或建议，欢迎通过以下方式联系：

- 📧 Email: 2294370973@qq.com
- 💬 Issues: [GitHub Issues](https://github.com/chen-kangkang/CalendarApplication/issues)

## 🙏 致谢

感谢所有为本项目做出贡献的开发者和用户！

## 🌐 GitHub Pages

本项目配置了GitHub Pages网站，用于展示应用的功能和截图。

### 网站内容

- 项目介绍和功能特性
- 应用界面截图预览
- 下载链接
- 技术栈展示

### 配置说明

网站文件位于 `docs/` 目录：
- `docs/index.html` - 网站主页
- `docs/assets/images/screenshots/` - 应用截图

### 更新网站

1. 修改 `docs/index.html` 文件
2. 添加或更新截图到 `docs/assets/images/screenshots/`
3. 提交到GitHub：
   ```bash
   git add docs/
   git commit -m "Update GitHub Pages"
   git push origin main
   ```
4. 等待几分钟，网站会自动更新

---

## 📈 更新日志

### v1.0.0 (2026-01-19)

- ✨ 实现完整的用户认证系统
- ✨ 实现四象限视图功能
- ✨ 实现日/周/月视图切换
- ✨ 实现日程的增删改查
- ✨ 实现重复日程功能
- ✨ 实现智能提醒功能
- ✨ 实现搜索与筛选功能
- ✨ 实现个人中心功能
- ✨ 实现密码修改功能
- 🌐 配置GitHub Pages网站
- 📱 支持Android 6.0+

---

**如果这个项目对你有帮助，请给个 ⭐ Star 支持一下！**

Made with ❤️ by chen-kangkang
