{{- define "platform.name" -}}
{{- .Chart.Name }}
{{- end }}

{{- define "platform.fullname" -}}
{{- .Release.Name }}
{{- end }}

{{- define "platform.waitDepsInit" -}}
- name: wait-deps
  image: busybox:1.36
  command:
    - sh
    - -c
    - |
      until nc -z config-server {{ .configPort }} && nc -z redis 6379; do
        echo "waiting for config-server and redis..."
        sleep 3
      done
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
