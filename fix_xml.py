import re

with open('f:\\AndriodProjects\\CalendarApplication\\app\\src\\main\\res\\layout\\activity_view.xml', 'r', encoding='utf-8') as f:
    content = f.read()

old_pattern = r'''                    <!-- 日程区域 -->
                    <LinearLayout
                        android:id="@+id/ll_day_schedule_area"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:orientation="vertical"
                        android:background="@android:color/white"
                        android:paddingLeft="8dp">
                        <!-- 时间格线 -->
                        <View
                            android:layout_width="match_parent"
                            android:layout_height="1dp"
                            android:background="@color/gray_light"/>
                        <LinearLayout
                            android:layout_width="match_parent"
                            android:layout_height="60dp"
                            android:orientation="vertical"
                            android:paddingVertical="2dp">
                            <!-- 日程项会动态添加在这里 -->
                        </LinearLayout>'''

new_content = '''                    <!-- 日程区域 -->
                    <FrameLayout
                        android:id="@+id/ll_day_schedule_area"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:minHeight="1440dp"
                        android:background="@android:color/white"
                        android:paddingLeft="8dp">

                        <LinearLayout
                            android:id="@+id/ll_day_time_slots"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="vertical">
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                            <View android:layout_width="match_parent" android:layout_height="60dp" android:background="@android:color/transparent"/>
                        </LinearLayout>

                        <LinearLayout
                            android:id="@+id/ll_day_schedule_container"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:orientation="vertical"/>

                    </FrameLayout>'''

if old_pattern in content:
    new_file_content = content.replace(old_pattern, new_content)
    with open('f:\\AndriodProjects\\CalendarApplication\\app\\src\\main\\res\\layout\\activity_view.xml', 'w', encoding='utf-8') as f:
        f.write(new_file_content)
    print("XML文件修复成功！")
else:
    print("未找到匹配的模式")
    # 查找包含日程区域的开始位置
    match = re.search(r'<!-- 日程区域 -->', content)
    if match:
        print(f"找到 '<!-- 日程区域 -->' 在位置: {match.start()}")
        print("前后内容:")
        start = max(0, match.start() - 100)
        end = min(len(content), match.start() + 500)
        print(content[start:end])
