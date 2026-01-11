require 'net/http'
require 'uri'

module Jekyll
  class GitHubCodeTag < Liquid::Tag
    def initialize(tag_name, markup, tokens)
      super

      args = markup.strip.split
      raise ArgumentError, 'Usage: {% github_code <url> <start-end> [lang:xyz] %}' if args.empty?

      @original_url = args[0]

      range = args[1] || ''
      if range =~ /(\d+)-(\d+)/
        @start_line = Regexp.last_match(1).to_i
        @end_line = Regexp.last_match(2).to_i
      else
        @start_line = 1
        @end_line = 999_999
      end

      @opts = {}
      args.drop(2).each do |part|
        key, value = part.split(':', 2)
        @opts[key] = value if key && value
      end
    end

    def render(context)
      raw_url = to_raw_url(@original_url)
      uri = URI.parse(raw_url)
      code_text = fetch(uri)

      lines = code_text.gsub("\r\n", "\n").gsub("\r", "\n").split("\n")
      total = lines.length
      s = [[@start_line, 1].max, total].min
      e = [[@end_line, 1].max, total].min

      return error_div("Invalid line range #{@start_line}-#{@end_line} for file with #{total} lines.") if s > e || s < 1 || e < 1

      slice = lines[(s - 1)..(e - 1)].join("\n")

      lang = (@opts['lang'] || guess_lang_from_path(uri.path)).to_s

      file_name = File.basename(uri.path)
      github_url = build_github_blob_url(@original_url, @start_line, @end_line)
      short_sha = extract_short_sha(@original_url)

      source_text = ''
      if github_url && short_sha
        source_text = "#{file_name}@#{short_sha} (L#{@start_line}-L#{@end_line})"
      end

      lang_hint = lang.empty? ? 'plaintext' : lang
      language_class = "language-#{lang_hint}"

      out = +''
      # Wrap in Rouge container so footer can sit bottom-right inside the box
      out << %Q(<div class="#{language_class} highlighter-rouge github-code-wrap" data-github-url="#{escape_attr(github_url)}" data-github-source="#{escape_attr(source_text)}">)
      # Rouge highlight markup with escaped code
      out << %Q(<div class="highlight"><pre class="highlight"><code class="#{language_class}" data-lang="#{html_escape(lang_hint)}">#{html_escape(slice)}</code></pre></div>)
      # Bottom-right footer link
      if github_url && short_sha
        link_label = "#{file_name}@#{short_sha}"
          out << %Q(<div class="github-code-footer">)
        out << %Q(<a class="github-code-link" href="#{escape_attr(github_url)}" target="_blank" rel="noopener">#{html_escape(link_label)}</a>)
        out << %Q(</div>)
      end
      out << %Q(</div>)
      out
    rescue => e
      error_div(e.message)
    end

    private

    def fetch(uri)
      http = Net::HTTP.new(uri.host, uri.port)
      http.use_ssl = (uri.scheme == 'https')
      http.read_timeout = 15
      req = Net::HTTP::Get.new(uri.request_uri)
      req['User-Agent'] = 'Jekyll-GitHubCodeTag'
      res = http.request(req)
      case res
      when Net::HTTPSuccess
        res.body.force_encoding('UTF-8')
      else
        raise "Failed to fetch #{uri} (#{res.code} #{res.message})"
      end
    end

    def to_raw_url(url)
      uri = URI.parse(url)
      return url if uri.host == 'raw.githubusercontent.com'

      if uri.host == 'github.com'
        parts = uri.path.split('/')
        if parts[3] == 'blob' && parts.size >= 6
          owner = parts[1]
          repo = parts[2]
          sha = parts[4]
          path = parts[5..-1].join('/')
          return "https://raw.githubusercontent.com/#{owner}/#{repo}/#{sha}/#{path}"
        end
      end

      url
    rescue
      url
    end

    def extract_short_sha(url)
      uri = URI.parse(url)
      parts = uri.path.split('/')

      if uri.host == 'raw.githubusercontent.com'
        sha = parts[3]
      elsif uri.host == 'github.com'
        sha = parts[4]
      else
        return nil
      end

      sha[0..6] if sha && sha.length >= 7
    rescue
      nil
    end

    def build_github_blob_url(url, start_line, end_line)
      uri = URI.parse(url)

      owner = nil
      repo = nil
      sha = nil
      path = nil

      if uri.host == 'raw.githubusercontent.com'
        parts = uri.path.split('/')
        owner = parts[1]
        repo = parts[2]
        sha = parts[3]
        path = parts[4..-1].join('/')
      elsif uri.host == 'github.com'
        parts = uri.path.split('/')
        owner = parts[1]
        repo = parts[2]
        sha = parts[4]
        path = parts[5..-1].join('/')
      else
        return nil
      end

      return nil unless owner && repo && sha && path

      "https://github.com/#{owner}/#{repo}/blob/#{sha}/#{path}#L#{start_line}-L#{end_line}"
    rescue
      nil
    end

    def guess_lang_from_path(path)
      ext = File.extname(path).downcase
      case ext
      when '.rb' then 'ruby'
      when '.md', '.markdown' then 'md'
      when '.java' then 'java'
      when '.kt' then 'kotlin'
      when '.groovy' then 'groovy'
      when '.py' then 'python'
      when '.js' then 'javascript'
      when '.ts' then 'typescript'
      when '.tsx' then 'tsx'
      when '.jsx' then 'jsx'
      when '.json' then 'json'
      when '.yml', '.yaml' then 'yaml'
      when '.xml' then 'xml'
      when '.html', '.htm' then 'html'
      when '.css' then 'css'
      when '.sql' then 'sql'
      else ''
      end
    end

    def html_escape(s)
      s.to_s
       .gsub('&', '&amp;')
       .gsub('<', '&lt;')
       .gsub('>', '&gt;')
       .gsub('"', '&quot;')
    end

    def escape_attr(s)
      s.to_s
       .gsub('&', '&amp;')
       .gsub('"', '&quot;')
       .gsub("'", '&#39;')
       .gsub('<', '&lt;')
       .gsub('>', '&gt;')
    end

    def error_div(message)
      %Q(<div class="github-code-error">#{html_escape(message)}</div>)
    end
  end
end

Liquid::Template.register_tag('github_code', Jekyll::GitHubCodeTag)
