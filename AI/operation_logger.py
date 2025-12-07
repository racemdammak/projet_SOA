"""
Module de logging des opérations dans un fichier XML
"""
import xml.etree.ElementTree as ET
from datetime import datetime
from pathlib import Path
from typing import Optional

class OperationLogger:    
    LOG_FILE = "../CloudWS/operations_log.xml"
    DATE_FORMAT = "%Y-%m-%dT%H:%M:%S"
    
    class OperationType:
        SUMMARIZE = "RESUMER"
    
    class OperationStatus:
        SUCCESS = "SUCCESS"
        ERROR = "ERROR"
    
    @staticmethod
    def log_operation(operation_type: str, filename: str, status: str, message: str, file_size: Optional[int] = None):
        root = OperationLogger._get_or_create_root()
        
        operation = ET.SubElement(root, "operation")
        
        operation_id = datetime.now().strftime("%Y%m%d%H%M%S%f")[:-3] + "_" + operation_type
        operation.set("id", operation_id)
        
        timestamp = ET.SubElement(operation, "timestamp")
        timestamp.text = datetime.now().strftime(OperationLogger.DATE_FORMAT)
        
        type_elem = ET.SubElement(operation, "type")
        type_elem.text = operation_type
        
        filename_elem = ET.SubElement(operation, "filename")
        filename_elem.text = filename if filename else ""
        
        status_elem = ET.SubElement(operation, "status")
        status_elem.text = status
        
        message_elem = ET.SubElement(operation, "message")
        message_elem.text = message if message else ""
        
        if file_size is not None and file_size > 0:
            file_size_elem = ET.SubElement(operation, "fileSize")
            file_size_elem.text = str(file_size)
        
        OperationLogger._save_document(root)
    
    @staticmethod
    def _get_or_create_root():
        log_file = Path(OperationLogger.LOG_FILE)
        
        if log_file.exists() and log_file.stat().st_size > 0:
            tree = ET.parse(log_file)
            root = tree.getroot()
            return root
        
        root = ET.Element("operations")
        root.set("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
        root.set("xsi:noNamespaceSchemaLocation", "operations_log.xsd")
        return root
    
    @staticmethod
    def _save_document(root):
        log_file = Path(OperationLogger.LOG_FILE)
        ET.indent(root, space="  ")
        tree = ET.ElementTree(root)
        tree.write(log_file, encoding="utf-8", xml_declaration=True)

