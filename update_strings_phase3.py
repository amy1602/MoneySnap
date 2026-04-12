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
    "add_transaction_title": "Add Transaction",
    "edit_transaction_title": "Edit Transaction",
    "add_transaction_enter_amount": "ENTER AMOUNT",
    "add_transaction_note_hint": "What was this for?",
    "add_transaction_save": "Save Transaction",
    "add_transaction_update": "Update Transaction",
    "add_transaction_select_category": "Select Category",
    "add_transaction_no_categories": "No categories found",
    "transaction_detail_title": "Transaction details",
    "transaction_detail_unknown_category": "Unknown Category",
    "transaction_detail_date": "Date",
    "transaction_detail_type": "Type",
    "transaction_detail_notes": "Notes",
    "transaction_detail_none": "None",
    "transaction_detail_edit": "Edit",
    "add_category_title": "Add Category",
    "edit_category_title": "Edit Category",
    "add_category_name_label": "Category name",
    "add_category_name_hint": "e.g. Cinema",
    "add_category_select_icon": "Select Icon",
    "add_category_color": "Category Color",
    "add_category_save": "Save Category",
    "add_category_update": "Update Category",
    "common_expense": "Expense",
    "common_income": "Income",
    "common_ok": "OK",
    "common_cancel": "Cancel",
    "common_delete": "Delete"
}

vi_strings = {
    "add_transaction_title": "Thêm giao dịch",
    "edit_transaction_title": "Sửa giao dịch",
    "add_transaction_enter_amount": "NHẬP SỐ TIỀN",
    "add_transaction_note_hint": "Ghi chú cho khoản này?",
    "add_transaction_save": "Lưu giao dịch",
    "add_transaction_update": "Cập nhật giao dịch",
    "add_transaction_select_category": "Chọn danh mục",
    "add_transaction_no_categories": "Không tìm thấy danh mục nào",
    "transaction_detail_title": "Chi tiết giao dịch",
    "transaction_detail_unknown_category": "Danh mục không rõ",
    "transaction_detail_date": "Ngày",
    "transaction_detail_type": "Loại",
    "transaction_detail_notes": "Ghi chú",
    "transaction_detail_none": "Trống",
    "transaction_detail_edit": "Sửa",
    "add_category_title": "Thêm danh mục",
    "edit_category_title": "Sửa danh mục",
    "add_category_name_label": "Tên danh mục",
    "add_category_name_hint": "Ví dụ: Xem phim",
    "add_category_select_icon": "Chọn biểu tượng",
    "add_category_color": "Màu sắc danh mục",
    "add_category_save": "Lưu danh mục",
    "add_category_update": "Cập nhật danh mục",
    "common_expense": "Khoản chi",
    "common_income": "Khoản thu",
    "common_ok": "Đồng ý",
    "common_cancel": "Hủy",
    "common_delete": "Xóa"
}

merge_strings('app/src/main/res/values/strings.xml', en_strings)
merge_strings('app/src/main/res/values-vi/strings.xml', vi_strings)
