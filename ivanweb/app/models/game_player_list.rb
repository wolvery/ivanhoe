class GamePlayerList < ActiveRecord::Base
  set_table_name "player_game_role"   
  
  def self.get_players( game_id )
    players_and_roles = find(:all, :conditions => [ "fk_game_id = ?", game_id ] )
    raw_players = []
    players_and_roles.each do |list_row|
      raw_players << Player.find(list_row.fk_player_id)
    end
    
    # pull out the players who are members
    players = []
    raw_players.map { |player|   
      if( !players.member? player )
       players << player
      end
    }
    
    # sort them by last name
    players.sort! { |x,y|  x.lname <=> y.lname }
    
    players
  end
  
  def self.get_games( player_id )
   return [] if not player_id

   game_ids = find(:all, :conditions => [ "fk_player_id = ?", player_id ] )   

   # only get games which have this role participating in play
   game_ids = game_ids.select { |game_id|
    Move.find( :first, :conditions => ["fk_role_id = ?",game_id.fk_role_id ] ) != nil  
   }

   games = []
   game_ids.each do |game_id|
    begin
      game = Game.find(game_id.fk_game_id)
      games << game    
    rescue
      # ignore games that no longer exist
    end
   end
   
   games
  end
  
  def self.get_roles( game_id )
    players_and_roles = find(:all, :conditions => [ "fk_game_id = ?", game_id ] )

    roles = []
    players_and_roles.each do |list_row|
      roles << Role.find(list_row.fk_role_id)
    end
  
    roles
  end
    
end
