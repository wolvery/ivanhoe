require 'digest/md5'

# this model expects a certain database layout and its based on the name/login pattern. 
class Player < ActiveRecord::Base
  set_table_name "player"  
 
  def self.authenticate(login, pass)
    find( :first, :conditions => ["playername = ? AND password = ?", login, md5(pass)])
  end  

  def change_password(pass)
    update_attribute "password", self.class.md5(pass)
  end
  
  def can_edit_game?(game_id)
    game = Game.find(game_id)   
    return ( game and game.fk_creator_id == @id and @new_game_permission )
  end
    
  protected

  def self.md5(pass)
    Digest::MD5.hexdigest(pass)
  end
    
  before_create :crypt_password
  
  def crypt_password
    write_attribute("password", self.class.md5(password))
  end

  validates_length_of :playername, :within => 1..40
  validates_length_of :password, :within => 1..40
  validates_presence_of :playername, :password, :password_confirmation
  validates_uniqueness_of :playername, :on => :create
  validates_confirmation_of :password, :on => :create     
end
