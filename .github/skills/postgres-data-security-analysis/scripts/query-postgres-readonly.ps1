param(
    [Parameter(Mandatory = $true)]
    [string]$Sql,

    [string]$ContainerName = "digitalsign-db"
)

$trimmedSql = $Sql.Trim()

if ([string]::IsNullOrWhiteSpace($trimmedSql)) {
    throw "SQL cannot be empty."
}

while ($trimmedSql.EndsWith(";")) {
    $trimmedSql = $trimmedSql.Substring(0, $trimmedSql.Length - 1).TrimEnd()
}

if ($trimmedSql -match ";") {
    throw "Only a single SELECT or WITH statement is allowed."
}

if ($trimmedSql -notmatch "^(?is)(select|with)\b") {
    throw "Only SELECT or WITH queries are allowed."
}

$forbiddenPattern = "(?is)\b(insert|update|delete|merge|alter|drop|truncate|grant|revoke|create|comment|copy|call|vacuum|analyze|refresh|reindex|do)\b"
if ($trimmedSql -match $forbiddenPattern) {
    throw "Forbidden SQL verb detected. Only read-only queries are allowed."
}

$containerId = docker ps --filter "name=$ContainerName" --format "{{.ID}}"
if ([string]::IsNullOrWhiteSpace($containerId)) {
    throw "Container '$ContainerName' is not running."
}

$sqlBlock = @"
BEGIN READ ONLY;
SET LOCAL statement_timeout = '15s';
$trimmedSql
ROLLBACK;
"@

$sqlBlock | docker exec -i $ContainerName sh -lc 'PGPASSWORD="$POSTGRES_PASSWORD" psql -X -v ON_ERROR_STOP=1 -P pager=off -U "$POSTGRES_USER" -d "$POSTGRES_DB"'