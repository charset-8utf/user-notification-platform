{{- define "platform.name" -}}
{{- .Chart.Name }}
{{- end }}

{{- define "platform.fullname" -}}
{{- .Release.Name }}
{{- end }}

{{- define "platform.waitDepsInit" -}}
- name: wait-deps
  image: busybox:1.36@sha256:73aaf090f3d85aa34ee199857f03fa3a95c8ede2ffd4cc2cdb5b94e566b11662
  securityContext:
    allowPrivilegeEscalation: false
    runAsNonRoot: true
    runAsUser: {{ .root.Values.podSecurity.runAsUser | default 1000 }}
    runAsGroup: {{ .root.Values.podSecurity.runAsGroup | default 1000 }}
    capabilities:
      drop:
        - ALL
  command:
    - sh
    - -c
    - |
      until nc -z config-server {{ .configPort }} && nc -z redis 6379{{- if .waitSchemaRegistry }} && nc -z schema-registry 8081{{- end }}; do
        echo "waiting for dependencies..."
        sleep 3
      done
{{- end }}

{{- define "platform.podSecurityContext" -}}
runAsNonRoot: true
runAsUser: {{ .Values.podSecurity.runAsUser | default 1000 }}
runAsGroup: {{ .Values.podSecurity.runAsGroup | default 1000 }}
fsGroup: {{ .Values.podSecurity.fsGroup | default 1000 }}
seccompProfile:
  type: RuntimeDefault
{{- end }}

{{- define "platform.containerSecurityContext" -}}
allowPrivilegeEscalation: false
readOnlyRootFilesystem: true
runAsNonRoot: true
runAsUser: {{ .Values.podSecurity.runAsUser | default 1000 }}
runAsGroup: {{ .Values.podSecurity.runAsGroup | default 1000 }}
capabilities:
  drop:
    - ALL
{{- end }}

{{- define "platform.tmpVolumeMount" -}}
- name: tmp
  mountPath: /tmp
{{- end }}

{{- define "platform.tmpVolume" -}}
- name: tmp
  emptyDir: {}
{{- end }}

{{- define "platform.resources" -}}
{{- $resources := index .Values.resources .key -}}
resources:
  requests:
    cpu: {{ $resources.requests.cpu | quote }}
    memory: {{ $resources.requests.memory | quote }}
  limits:
    cpu: {{ $resources.limits.cpu | quote }}
    memory: {{ $resources.limits.memory | quote }}
{{- end }}

{{- define "platform.actuatorProbes" -}}
startupProbe:
  httpGet:
    path: /actuator/health
    port: {{ .port }}
  failureThreshold: {{ .startupFailureThreshold | default 60 }}
  periodSeconds: {{ .startupPeriodSeconds | default 10 }}
  timeoutSeconds: 5
readinessProbe:
  httpGet:
    path: /actuator/health
    port: {{ .port }}
  periodSeconds: 10
  failureThreshold: 6
  timeoutSeconds: 5
livenessProbe:
  httpGet:
    path: /actuator/health
    port: {{ .port }}
  periodSeconds: 30
  failureThreshold: 3
  timeoutSeconds: 5
{{- end }}

{{- define "platform.podLifecycle" -}}
terminationGracePeriodSeconds: {{ .Values.lifecycle.terminationGracePeriodSeconds | default 60 }}
{{- end }}

{{- define "platform.preStop" -}}
lifecycle:
  preStop:
    exec:
      command:
        - sh
        - -c
        - sleep {{ .Values.lifecycle.preStopSleepSeconds | default 10 }}
{{- end }}

{{- define "platform.image" -}}
{{- $registry := .root.Values.global.registry -}}
{{- $owner := .root.Values.global.imageOwner -}}
{{- $image := .image -}}
{{- $tag := .root.Values.global.imageTag | default (.tag | default "latest") -}}
{{- if and $registry $owner -}}
{{- printf "%s/%s/%s:%s" $registry $owner $image $tag -}}
{{- else -}}
{{- printf "%s:%s" $image $tag -}}
{{- end -}}
{{- end }}
