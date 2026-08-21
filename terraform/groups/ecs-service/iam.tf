resource "aws_iam_role" "task_role" {
  name               = "${var.environment}-${local.service_name}-task-role"
  path               = "/"
  assume_role_policy = data.aws_iam_policy_document.task_assume.json

  tags = {
    Name               = "${var.environment}-${local.service_name}-task-role"
    Environment        = var.environment
    ECSClusterName     = "${local.name_prefix}-cluster"
    ManagedByTerraform = "true"
  }
}

resource "aws_iam_policy" "task_policy" {
  name        = "${var.environment}-${local.service_name}-task-policy"
  policy      = data.aws_iam_policy_document.task_policy.json

  tags = {
    Name               = "${var.environment}-${local.service_name}-task-policy"
    Environment        = var.environment
    ECSClusterName     = "${local.name_prefix}-cluster"
    ManagedByTerraform = "true"
  }
}

resource "aws_iam_role_policy_attachment" "task_role_attachment" {
  role       = aws_iam_role.task_role.name
  policy_arn = aws_iam_policy.task_policy.arn
}
