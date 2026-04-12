import xml.etree.ElementTree as ET
import os

def merge_strings(file_path, new_strings):
    if os.path.exists(file_path):
        tree = ET.parse(file_path)
        root = tree.getroot()
    else:
        root = ET.Element('resources')
        tree = ET.ElementTree(root)

    existing_keys = {child.attrib.get('name') for child in root if child.tag == 'string'}
    
    for key, value in new_strings.items():
        if key not in existing_keys:
            element = ET.SubElement(root, 'string', {'name': key})
            element.text = value
        else:
            # Update existing if needed, or skip. Here we just skip to avoid duplicates.
            pass
            
    ET.indent(tree, space="    ", level=0)
    tree.write(file_path, encoding='utf-8', xml_declaration=True)

en_strings = {
    "profile_title": "Profile",
    "profile_sign_out": "Sign Out",
    "profile_sign_out_message": "Are you sure you want to sign out of your account?",
    "profile_edit_profile": "Edit Profile",
    "profile_account_actions": "ACCOUNT ACTIONS",
    "profile_export_excel": "Export to Excel",
    "profile_export_subtitle": "Download your financial reports",
    "profile_share_excel": "Share Excel Report",
    "profile_manage_categories": "Manage your spending categories",
    "profile_account_settings": "Account Settings",
    "profile_settings_subtitle": "Security, notifications, and privacy",
    "profile_logout_subtitle": "Sign out of your account",
    "profile_exporting": "Exporting Excel file...",
    "settings_security_access": "SECURITY & ACCESS",
    "settings_change_password": "Change Password",
    "settings_biometric": "Biometric Authentication",
    "settings_biometric_subtitle": "Touch ID or Face ID",
    "settings_preferences": "PREFERENCES",
    "settings_language_subtitle": "English (United States)",
    "settings_password_success": "Password updated successfully",
    "settings_password_wrong": "Current password is wrong",
    "settings_unknown_error": "Unknown error occurred",
    "change_name_title": "Change Name",
    "change_name_current": "CURRENT NAME",
    "change_name_new": "NEW NAME",
    "change_name_hint": "Enter new name",
    "change_name_visibility_notice": "This name will be visible across your shared accounts and reports.",
    "change_name_verification_notice": "Profile verification is active. Your official ledger name will be updated instantly.",
    "change_name_save": "Save Changes",
    "select_avatar_title": "Select Avatar",
    "select_avatar_collection": "DEFAULT COLLECTION",
    "select_avatar_subtitle": "Choose a fluffy feline companion that represents your personality on the ledger.",
    "update_password_title": "Update Password",
    "update_password_subtitle": "Keep your account protected",
    "update_password_current": "CURRENT PASSWORD",
    "update_password_new": "NEW PASSWORD",
    "update_password_confirm": "CONFIRM NEW PASSWORD",
    "update_password_min_chars": "Minimum 8 characters",
    "update_password_match_hint": "Must match new password",
    "update_password_security_notice": "Use at least 8 characters with a mix of letters, numbers, and symbols to ensure maximum security for your money manager.",
    "update_password_enter_current": "Please enter your current password",
    "update_password_min_error": "New password must be at least 8 characters",
    "update_password_match_error": "Passwords do not match"
}

vi_strings = {
    "profile_title": "Hồ sơ",
    "profile_sign_out": "Đăng xuất",
    "profile_sign_out_message": "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản không?",
    "profile_edit_profile": "Sửa hồ sơ",
    "profile_account_actions": "HÀNH ĐỘNG TÀI KHOẢN",
    "profile_export_excel": "Xuất file Excel",
    "profile_export_subtitle": "Tải về báo cáo tài chính của bạn",
    "profile_share_excel": "Chia sẻ báo cáo Excel",
    "profile_manage_categories": "Quản lý các danh mục chi tiêu của bạn",
    "profile_account_settings": "Cài đặt tài khoản",
    "profile_settings_subtitle": "Bảo mật, thông báo và quyền riêng tư",
    "profile_logout_subtitle": "Đăng xuất khỏi tài khoản của bạn",
    "profile_exporting": "Đang xuất file Excel...",
    "settings_security_access": "BẢO MẬT & TRUY CẬP",
    "settings_change_password": "Đổi mật khẩu",
    "settings_biometric": "Xác thực sinh trắc học",
    "settings_biometric_subtitle": "Touch ID hoặc Face ID",
    "settings_preferences": "TÙY CHỈNH",
    "settings_language_subtitle": "Tiếng Việt (Việt Nam)",
    "settings_password_success": "Cập nhật mật khẩu thành công",
    "settings_password_wrong": "Mật khẩu hiện tại không đúng",
    "settings_unknown_error": "Đã xảy ra lỗi không xác định",
    "change_name_title": "Đổi tên",
    "change_name_current": "TÊN HIỆN TẠI",
    "change_name_new": "TÊN MỚI",
    "change_name_hint": "Nhập tên mới",
    "change_name_visibility_notice": "Tên này sẽ hiển thị trên tất cả tài khoản chung và báo cáo.",
    "change_name_verification_notice": "Xác minh hồ sơ đang hoạt động. Tên chính thức của bạn sẽ được cập nhật ngay lập tức.",
    "change_name_save": "Lưu thay đổi",
    "select_avatar_title": "Chọn ảnh đại diện",
    "select_avatar_collection": "BỘ SƯU TẬP MẶC ĐỊNH",
    "select_avatar_subtitle": "Hãy chọn một người bạn mèo bông đại diện cho cá tính của bạn trên sổ thu chi.",
    "update_password_title": "Cập nhật mật khẩu",
    "update_password_subtitle": "Giữ cho tài khoản của bạn luôn được bảo vệ",
    "update_password_current": "MẬT KHẨU HIỆN TẠI",
    "update_password_new": "MẬT KHẨU MỚI",
    "update_password_confirm": "XÁC NHẬN MẬT KHẨU MỚI",
    "update_password_min_chars": "Tối thiểu 8 ký tự",
    "update_password_match_hint": "Phải khớp với mật khẩu mới",
    "update_password_security_notice": "Sử dụng ít nhất 8 ký tự bao gồm chữ cái, chữ số và biểu tượng để đảm bảo bảo mật tối đa cho trình quản lý tiền của bạn.",
    "update_password_enter_current": "Vui lòng nhập mật khẩu hiện tại",
    "update_password_min_error": "Mật khẩu mới phải có ít nhất 8 ký tự",
    "update_password_match_error": "Mật khẩu không khớp"
}

merge_strings('app/src/main/res/values/strings.xml', en_strings)
merge_strings('app/src/main/res/values-vi/strings.xml', vi_strings)
