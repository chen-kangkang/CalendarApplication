with open('f:\\AndriodProjects\\CalendarApplication\\app\\src\\main\\res\\layout\\activity_view.xml', 'r', encoding='utf-8') as f:
    content = f.read()

# 查找FrameLayout结束后的残留内容并清理
# 残留内容从 </FrameLayout> 开始，到 </LinearLayout> </ScrollView> 结束

import re

# 找到FrameLayout的结束位置
frame_layout_end = content.find('</FrameLayout>')

if frame_layout_end != -1:
    print(f"找到FrameLayout结束位置: {frame_layout_end}")
    
    # 找到紧接着的</LinearLayout>和</ScrollView>
    next_section = content.find('</LinearLayout>', frame_layout_end)
    scroll_view_end = content.find('</ScrollView>', frame_layout_end)
    
    if next_section != -1 and scroll_view_end != -1:
        print(f"找到残留内容结束位置: {scroll_view_end}")
        
        # 提取需要删除的内容范围
        start_delete = frame_layout_end + len('</FrameLayout>')
        end_delete = scroll_view_end + len('</ScrollView>')
        
        # 显示要删除的内容
        to_delete = content[start_delete:end_delete]
        print(f"\n要删除的残留内容长度: {len(to_delete)} 字符")
        print(f"内容预览:\n{to_delete[:500]}...")
        
        # 删除残留内容
        new_content = content[:start_delete] + content[end_delete:]
        
        with open('f:\\AndriodProjects\\CalendarApplication\\app\\src\\main\\res\\layout\\activity_view.xml', 'w', encoding='utf-8') as f:
            f.write(new_content)
        
        print("\n残留内容已清理！")
        print(f"文件大小变化: {len(content)} -> {len(new_content)} 字符")
    else:
        print("未找到残留内容结束位置")
        print(f"next_section: {next_section}, scroll_view_end: {scroll_view_end}")
else:
    print("未找到FrameLayout结束标签")
