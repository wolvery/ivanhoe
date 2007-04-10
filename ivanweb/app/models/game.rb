
class Game < ActiveRecord::Base
  set_table_name "game"   
  
  validates_length_of :name, :within => 1..100

  attr_accessor :guest_codes
  
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
  
  # can this player play in this game?
  def can_play?( player )
    return true if not restricted or ( player and player.admin )
    found = RestrictedGamePlayerList.find( :first, :conditions => ['fk_game_id = ? and fk_player_id = ?', id, player.id ]) if player
    found ? true : false
  end
  
  # take a set of player objects and convert to player id array in JSON format
  def encode_guests( players )
    guests = []
    players.each { |p| guests << p.id }
    @guest_codes = guests.to_json
  end
  
  # update the guest list based on the contents of the guest_codes
  def update_guest_list()
  
    # get the existing guest list from db
    existing_entries = RestrictedGamePlayerList.find( :all, :conditions => ['fk_game_id = ?', id ] )

    # the list of guests is transported in a JSON array
    guest_ids = JSON.parse(@guest_codes) if @guest_codes

    # add any entries not present in the db
    guest_ids.each { |guest_id|       
      matching_entry = existing_entries.select { |entry|
        entry.fk_player_id == guest_id.to_i
      }
      
      if matching_entry.size == 0 then
        list_entry = RestrictedGamePlayerList.new
        list_entry.fk_player_id = guest_id
        list_entry.fk_game_id = id
        list_entry.save
      end
    } if guest_ids
    
    # remove any entries not present in the guest_id list
    existing_entries.each { |existing_entry| 
       matching_entry = guest_ids.select { |id|
        existing_entry.fk_player_id == id.to_i
       } if guest_ids
       
       if matching_entry.size == 0 then
        RestrictedGamePlayerList.delete_all [ "fk_game_id = ? and fk_player_id = ?", existing_entry.fk_game_id, existing_entry.fk_player_id ]
       end
    }  
  end
  
  
end
