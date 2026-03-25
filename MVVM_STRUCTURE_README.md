<!-- 该文件用于说明当前项目建议采用的 MVVM 骨架结构，以及每个新增文件的职责边界。 -->
# KPappercutting MVVM 骨架说明

## 目标

这份骨架基于当前项目的实际规模设计，目标是先把页面中的状态、业务逻辑和数据访问拆开，逐步从“页面直接管状态”迁移到“Screen + ViewModel + Repository”。

## 当前建议结构

```text
app/src/main/java/com/example/kpappercutting/
├─ MainActivity.kt                         # 应用入口
├─ data/
│  ├─ model/
│  │  ├─ PaperCutModels.kt                # 现有基础数据模型
│  │  ├─ CreationDraft.kt                 # 创作草稿模型
│  │  └─ UserProfile.kt                   # 用户资料模型
│  └─ repository/
│     ├─ CreationRepository.kt            # 创作页数据接口
│     ├─ HomeRepository.kt                # 首页数据接口
│     ├─ CultureRepository.kt             # 文化页数据接口
│     ├─ CommunityRepository.kt           # 社区页数据接口
│     └─ ProfileRepository.kt             # 个人页数据接口
├─ ui/
│  ├─ AppSkeleton.kt                      # 应用页面骨架与导航容器
│  ├─ navigation/
│  │  └─ SteeringBottomBar.kt             # 底部导航
│  ├─ theme/                              # 主题配置
│  └─ features/
│     ├─ home/
│     │  ├─ HomeScreen.kt                 # 首页 UI
│     │  ├─ HomeViewModel.kt              # 首页状态管理
│     │  └─ HomeUiState.kt                # 首页状态定义
│     ├─ culture/
│     │  ├─ CultureScreen.kt              # 文化页 UI
│     │  ├─ CultureViewModel.kt           # 文化页状态管理
│     │  └─ CultureUiState.kt             # 文化页状态定义
│     ├─ creation/
│     │  ├─ CreateScreen.kt               # 创作页 UI
│     │  ├─ CreationState.kt.kt           # 现有旧文件，后续建议迁移或更名
│     │  ├─ CreateViewModel.kt            # 创作页状态管理
│     │  ├─ CreateUiState.kt              # 创作页状态定义
│     │  ├─ CreateUiAction.kt             # 创作页事件定义
│     │  ├─ CreateModels.kt               # 创作页专属模型
│     │  └─ component/
│     │     ├─ PaperCanvas.kt             # 画布组件骨架
│     │     ├─ TopControlBar.kt           # 顶部控制栏骨架
│     │     └─ BottomToolPalette.kt       # 底部工具栏骨架
│     ├─ community/
│     │  ├─ CommunityScreen.kt            # 社区页 UI
│     │  ├─ CommunityViewModel.kt         # 社区页状态管理
│     │  └─ CommunityUiState.kt           # 社区页状态定义
│     └─ profile/
│        ├─ ProfileScreen.kt              # 个人页 UI
│        ├─ ProfileViewModel.kt           # 个人页状态管理
│        └─ ProfileUiState.kt             # 个人页状态定义
└─ util/
   └─ Constants.kt                        # 项目通用常量
```

## 每层职责

### `ui`

- `Screen.kt` 类文件只负责渲染 UI 和转发用户交互。
- `UiState.kt` 负责集中定义页面渲染所需的状态。
- `ViewModel.kt` 负责处理用户操作、维护状态、调用仓库层。
- `component/` 用来拆分页面内部可复用的 UI 组件。

### `data`

- `model/` 存放可持久化或可复用的数据结构。
- `repository/` 定义页面或模块的数据访问接口。
- 后续如果接入本地数据库或网络接口，可以继续拆 `local/`、`remote/`。

### `util`

- 放项目级通用常量、扩展和工具函数。

## 创作页推荐迁移顺序

1. 先让 `CreateViewModel` 持有 `CreateUiState`。
2. 再把 `CreateScreen.kt` 里的本地状态逐步迁移到 `CreateViewModel`。
3. 然后把大组件拆到 `component/` 目录。
4. 最后把保存草稿、读取模板之类的数据访问放进 `CreationRepository`。

## 当前保留项

- `PaperCutModels.kt` 仍然保留，当前 `PaperShape` 继续复用。
- `CreationState.kt.kt` 目前没有删除，是为了避免直接影响现有 `CreateScreen.kt` 的编译；后续建议改名为 `EditTool.kt` 或把其中内容并入 `CreateModels.kt`。
