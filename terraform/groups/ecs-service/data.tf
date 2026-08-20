data "vault_generic_secret" "stack_secrets" {
  path = "applications/${var.aws_profile}/${var.environment}/${local.stack_name}-stack"
}

data "aws_kms_key" "kms_key" {
  key_id = local.kms_alias
}

data "vault_generic_secret" "service_secrets" {
  path = "applications/${var.aws_profile}/${var.environment}/${local.stack_name}-stack/${local.service_name}"
}

data "aws_vpc" "vpc" {
  filter {
    name   = "tag:Name"
    values = [local.vpc_name]
  }
}

# Get application subnet IDs
data "aws_subnets" "application" {
  filter {
    name   = "tag:Name"
    values = [local.application_subnet_pattern]
  }
}

data "aws_ecs_cluster" "ecs_cluster" {
  cluster_name = "${local.name_prefix}-cluster"
}

data "aws_iam_role" "ecs_cluster_iam_role" {
  name = "${local.name_prefix}-ecs-task-execution-role"
}

data "aws_lb" "service_lb" {
  name = "${var.environment}-chs-internalapi"
}

data "aws_lb_listener" "service_lb_listener" {
  load_balancer_arn = data.aws_lb.service_lb.arn
  port = 443
}


# retrieve all secrets for this stack using the stack path
data "aws_ssm_parameters_by_path" "secrets" {
  path = "/${local.name_prefix}"
}

# create a list of secrets names to retrieve them in a nicer format and lookup each secret by name
data "aws_ssm_parameter" "secret" {
  for_each = toset(data.aws_ssm_parameters_by_path.secrets.names)
  name = each.key
}

# retrieve all global secrets for this env using global path
data "aws_ssm_parameters_by_path" "global_secrets" {
  path = "/${local.global_prefix}"
}

# create a list of secrets names to retrieve them in a nicer format and lookup each secret by name
data "aws_ssm_parameter" "global_secret" {
  for_each = toset(data.aws_ssm_parameters_by_path.global_secrets.names)
  name     = each.key
}

// --- s3 bucket for shared services config ---
data "vault_generic_secret" "shared_s3" {
  path = "aws-accounts/shared-services/s3"
}

# IAM
data "aws_iam_policy_document" "task_assume" {
  statement {
    sid     = "AllowTaskAssumeRole"
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

data "aws_iam_policy_document" "task_policy" {
  statement {
    sid       = "AllowS3ListBuckets"
    effect    = "Allow"
    actions   = [
      "s3:ListBucket",
      "s3:GetBucketLocation"
    ]
    resources = [
      data.aws_s3_bucket.s3_av_bucket.arn,
      data.aws_s3_bucket.payments_reports_bucket.arn
    ]
  }

  statement {
    sid       = "AllowS3Objects"
    effect    = "Allow"
    actions   = [
      "s3:PutObject",
      "s3:GetObject",
      "s3:DeleteObject"
    ]
    resources = [
      "${data.aws_s3_bucket.s3_av_bucket.arn}/*",
      "${data.aws_s3_bucket.payments_reports_bucket.arn}/${var.environment}/*"
    ]
  }

  statement {
    sid       = "AllowSQSPushPull"
    effect    = "Allow"
    actions   = [
      "sqs:SendMessage",
      "sqs:SendMessageBatch"
    ]
    resources = [
      data.aws_sqs_queue.efs_doc_processor_queue.arn
    ]
  }
}

data "aws_s3_bucket" "s3_av_bucket" {
  bucket = var.s3_av_bucket_name
}

data "aws_s3_bucket" "payments_reports_bucket" {
  bucket = "${var.aws_profile}.${var.payments_reports_bucket_suffix}"
}

data "aws_sqs_queue" "efs_doc_processor_queue" {
  name = "efs-document-processor-${var.environment}-queue.fifo"
}
