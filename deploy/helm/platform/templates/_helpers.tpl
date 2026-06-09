{{- define "platform.name" -}}
{{- .Chart.Name }}
{{- end }}

{{- define "platform.fullname" -}}
{{- .Release.Name }}
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
