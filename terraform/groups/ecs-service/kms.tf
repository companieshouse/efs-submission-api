resource "aws_key_grant" "file_transfer_encryption_key_grant" {
  name              = "${var.environment}-${local.service_name}-key-grant"
  key_id            = data.aws_kms_alias.file_transfer_encryption_key_alias.target_key_id
  grantee_principal = aws_iam_role.task_role.arn
  operations        = ["Encrypt", "Decrypt", "GenerateDataKey"]
}
