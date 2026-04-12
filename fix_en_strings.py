import xml.etree.ElementTree as ET
import os

def merge_strings(file_path, new_strings):
    if os.path.exists(file_path):
        tree = ET.parse(file_path)
        root = tree.getroot()
    else:
        root = ET.Element('resources')
        tree = ET.ElementTree(root)

    existing_elements = {child.attrib.get('name'): child for child in root if child.tag == 'string'}
    
    # We want to insert them in order, or at least make sure they all exist.
    # To maintain some order, we can clear and rebuild if we have the full dict, 
    # but let's just append missing ones for now.
    
    for key, value in new_strings.items():
        if key not in existing_elements:
            element = ET.SubElement(root, 'string', {'name': key})
            element.text = value
        else:
            # Optionally update existing if they are placeholders? 
            # For now, if it exists, we assume it's correct English.
            pass
            
    ET.indent(tree, space="    ", level=0)
    tree.write(file_path, encoding='utf-8', xml_declaration=True)

full_en_strings = {
    "app_name": "MoneySnap",
    "splash_subtitle": "Smart tracking for the future",
    "splash_initializing": "INITIALIZING SECURITY",
    "splash_version": "Version 2.4.0",
    "biometric_scanning": "SCANNING...",
    "biometric_verified": "VERIFIED ✓",
    "biometric_cancelled": "CANCELLED",
    "biometric_try_again": "TRY AGAIN",
    "biometric_security": "Security",
    "biometric_check": "Check",
    "biometric_money": "MONEY",
    "biometric_manager": "MANAGER",
    "biometric_verify_identity": "Verify Identity",
    "biometric_instruction": "Place your finger on the sensor or use\nFace ID to continue",
    "biometric_cancel": "Cancel",
    "biometric_not_supported": "Biometric not supported on this device",
    "biometric_auth_cancelled": "Authentication cancelled",
    "biometric_auth_failed": "Authentication failed. Please try again.",
    "biometric_prompt_title": "Enable Biometric Login",
    "biometric_prompt_subtitle": "Verify fingerprint to enable biometric login",
    "biometric_no_sensor": "No biometric sensor detected",
    "biometric_unavailable": "Biometric sensor is currently unavailable",
    "biometric_none_enrolled": "No fingerprints enrolled. Please add them in your device settings.",
    "login_biometric_not_available": "Biometric not available",
    "login_biometric_title": "Login with Biometric",
    "login_biometric_subtitle": "Use fingerprint to sign in",
    "login_use_password": "Use Password",
    "login_fingerprint_not_recognized": "Fingerprint not recognized",
    "login_failed": "Login failed",
    "login_welcome_back": "Welcome Back",
    "login_instruction": "Please enter your details to sign in",
    "login_email_hint": "name@example.com",
    "login_password_hint": "Enter password",
    "login_forgot_password": "Forgot Password?",
    "login_button": "Login",
    "login_or_continue_with": "Or continue with",
    "login_unexpected_credential": "Unexpected credential type",
    "login_google_failed": "Google Sign-In failed: ",
    "error_prefix": "Error: ",
    "login_google_btn": "Google",
    "login_dont_have_account": "Don't have an account? ",
    "login_register_now": "Register Now",
    "register_failed": "Registration failed",
    "register_create_account": "Create Account",
    "register_instruction": "Start tracking your spending and saving today",
    "register_name_hint": "John Doe",
    "password_hint": "Password",
    "confirm_password_hint": "Confirm Password",
    "register_terms": "I agree to the Terms of Service & Privacy Policy",
    "register_button": "Register",
    "register_or_with": "Or register with",
    "register_already_have_account": "Already have an account? ",
    "home_total_balance": "Total Balance",
    "home_income": "INCOME",
    "home_expenses": "EXPENSES",
    "home_weekly_spending": "Weekly Spending",
    "home_last_7_days": "Last 7 days",
    "home_recent_transactions": "Recent Transactions",
    "home_view_all": "View All",
    "home_no_recent_transactions": "No recent transactions",
    "home_tab_home": "Home",
    "home_tab_history": "History",
    "home_tab_reports": "Reports",
    "home_tab_profile": "Profile",
    "report_net_balance": "NET BALANCE",
    "report_income": "Income",
    "report_expense": "Expense",
    "report_flow_analysis": "FLOW ANALYSIS",
    "report_category_spending": "CATEGORY SPENDING",
    "report_total": "TOTAL",
    "report_unknown": "Unknown",
    "report_significant_outflow": "SIGNIFICANT OUTFLOW",
    "report_expense_default": "Expense",
    "report_month": "Month",
    "report_year": "Year",
    "report_prev_month": "Prev Month",
    "report_next_month": "Next Month",
    "history_title": "Transaction History",
    "history_all_time": "All Time",
    "history_all_categories": "All Categories",
    "history_apply": "Apply",
    "history_clear": "Clear",
    "history_no_transactions": "No transactions found",
    "history_delete_title": "Delete Transaction",
    "history_delete_message": "Are you sure you want to delete this transaction? Your total balance will be updated. This action cannot be undone.",
    "history_delete_btn": "Delete",
    "history_cancel_btn": "Cancel",
    "history_unknown": "Unknown",
    "categories_title": "Categories",
    "categories_expense": "EXPENSE CATEGORIES",
    "categories_income": "INCOME CATEGORIES",
    "categories_empty_message": "No categories yet. Create some!",
    "categories_delete_title": "Delete Category",
    "categories_delete_message": "Are you sure you want to delete category \"%s\"? This action cannot be undone.",
    "categories_delete_btn": "Delete",
    "categories_cancel_btn": "Cancel",
    "categories_transactions_placeholder": "0 transactions this month",
}

merge_strings('app/src/main/res/values/strings.xml', full_en_strings)
