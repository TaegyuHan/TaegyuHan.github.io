require 'net/http'
require 'uri'

    def render(context)
      raw_url = to_raw_url(@original_url)
      uri = URI.parse(raw_url)
      code_text = fetch(uri)

      # Normalize line endings and slice
      lines = code_text.gsub("\r\n", "\n").gsub("\r", "\n").split("\n")
      total = lines.length
      s = [[@start_line, 1].max, total].min
      e = [[@end_line, 1].max, total].min

      if s > e || s < 1 || e < 1
        return error_div("Invalid line range #{@start_line}-#{@end_line} for file with #{total} lines.")
      end

      slice = lines[(s - 1)..(e - 1)].join("\n")

      lang  = (@opts['lang'] || guess_lang_from_path(uri.path)).to_s
      
      # Extract file name and build GitHub blob URL with line anchor
      file_name = File.basename(uri.path)
      github_url = build_github_blob_url(@original_url, @start_line, @end_line)
      short_sha = extract_short_sha(@original_url)
      
      # Build source info
      source_text = ""
      if github_url && short_sha
        source_text = "#{file_name}@#{short_sha} (L#{@start_line}-L#{@end_line})"
      end

      # Prefer Markdown code fences so kramdown+Rouge handle highlighting uniformly
      fence = '```'
      fence = '~~~~' if slice.include?('```')
      lang_hint = lang.empty? ? '' : lang

      out = +""
      # Emit a metadata div BEFORE the code block so JS can read it
      if github_url && source_text
        out << %Q(<div class="github-code-meta" data-github-source="#{escape_attr(source_text)}" data-github-url="#{escape_attr(github_url)}"></div>\n)
      end
      out << fence << lang_hint << "\n"
      out << slice
      out << "\n" << fence << "\n"
      out
    rescue => e
      error_div(e.message)
    end

      lang  = (@opts['lang'] || guess_lang_from_path(uri.path)).to_s
      
      # Extract file name and build GitHub blob URL with line anchor
      file_name = File.basename(uri.path)
      github_url = build_github_blob_url(@original_url, @start_line, @end_line)
      short_sha = extract_short_sha(@original_url)
      
      # Build caption with file name, short SHA, and line range
      caption = ""
      if github_url && short_sha
        caption_text = "#{file_name}@#{short_sha} (L#{@start_line}-L#{@end_line})"
        caption = %Q(<div class="code-caption"><a href="#{escape_attr(github_url)}" target="_blank" rel="noopener">#{html_escape(caption_text)}</a></div>\n)
      end

      # Prefer Markdown code fences so kramdown+Rouge handle highlighting uniformly
      fence = '```'
      fence = '~~~~' if slice.include?('```')
      lang_hint = lang.empty? ? '' : lang

      out = +""
      out << caption
      out << fence << lang_hint << "\n"
      out << slice
      out << "\n" << fence << "\n"
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
      # Convert github.com URLs like:
      # https://github.com/owner/repo/blob/<sha>/path -> raw
      if uri.host == 'github.com'
        parts = uri.path.split('/')
        # ['', owner, repo, 'blob', sha, path...]
        if parts[3] == 'blob' && parts.size >= 6
          owner = parts[1]
          repo  = parts[2]
          sha   = parts[4]
          path  = parts[5..-1].join('/')
          return "https://raw.githubusercontent.com/#{owner}/#{repo}/#{sha}/#{path}"
        end
      end
      url
    rescue
      url
    end

    def extract_short_sha(url)
      # Extract commit SHA (full or short) from URL
      # Handles both:
      # - raw.githubusercontent.com/.../owner/repo/<sha>/path
      # - github.com/owner/repo/blob/<sha>/path
      uri = URI.parse(url)
      parts = uri.path.split('/')
      
      if uri.host == 'raw.githubusercontent.com'
        # raw.githubusercontent.com/owner/repo/<sha>/path -> parts[3]
        sha = parts[3]
      elsif uri.host == 'github.com'
        # github.com/owner/repo/blob/<sha>/path -> parts[4]
        sha = parts[4]
      else
        return nil
      end
      
      # Return first 7 chars of SHA
      sha[0..6] if sha && sha.length >= 7
    rescue
      nil
    end

    def build_github_blob_url(url, start_line, end_line)
      # Convert to GitHub blob URL with line anchors
      # Result: https://github.com/owner/repo/blob/<sha>/path#L<start>-L<end>
      uri = URI.parse(url)
      
      owner = nil
      repo = nil
      sha = nil
      path = nil
      
      if uri.host == 'raw.githubusercontent.com'
        # raw.githubusercontent.com/owner/repo/<sha>/path
        parts = uri.path.split('/')
        owner = parts[1]
        repo  = parts[2]
        sha   = parts[3]
        path  = parts[4..-1].join('/')
      elsif uri.host == 'github.com'
        # github.com/owner/repo/blob/<sha>/path
        parts = uri.path.split('/')
        owner = parts[1]
        repo  = parts[2]
        sha   = parts[4]
        path  = parts[5..-1].join('/')
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
      when '.rb'        then 'ruby'
      when '.md'        then 'md'
      when '.markdown'  then 'md'
      when '.java'      then 'java'
      when '.kt'        then 'kotlin'
      when '.groovy'    then 'groovy'
      when '.py'        then 'python'
      when '.js'        then 'javascript'
      when '.ts'        then 'typescript'
      when '.tsx'       then 'tsx'
      when '.jsx'       then 'jsx'
      when '.json'      then 'json'
      when '.yml', '.yaml' then 'yaml'
      when '.xml'       then 'xml'
      when '.html', '.htm' then 'html'
      when '.css'       then 'css'
      when '.sql'       then 'sql'
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
