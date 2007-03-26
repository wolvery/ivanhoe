class Game < ActiveRecord::Base
  set_table_name "game"   
  
  def self.active_games
    
    # get all the moves that happend since the cut off date
    moves = Move.find( :all, :order => "submit_date DESC")
    
    # figure out which games they belong to
    games = []
    moves.each do |move| 
      game_id = move.fk_game_id
      
      begin
        game = Game.find(game_id)
        if games.size < 5 and not games.include? game then
          games << game
        end     
      rescue
        # ignore games that no longer exist
      end
      
    end
    
    games
  end
  
  validates_length_of :name, :within => 1..100
  
end
