require 'rexml/document'

class Journal

  attr_accessor :contents

  def initialize( content )
    @contents = content
  end
  
  def self.empty_journal()
    Journal.new("<p><i>No Journal Entry</i></p>")
  end

  def self.find( game_id, player_name )
    
    begin
      # open the journal file from the ivanhoe server dir
      document = REXML::Document.new( File.open("server/#{player_name}-game#{game_id}-journal.html") )    
      body = document.root.elements['body']
    
      # pull out the content of the <body> tag and stick it in a <div>
      div = REXML::Element.new('div')
      body.elements.each { |e| div.add e }
      contents = ""
      div.write( contents )
      Journal.new(contents) 
    rescue
      # if the file can't be opened or parsed, return blank
      self.empty_journal()
    end      
      
  end
 
end
