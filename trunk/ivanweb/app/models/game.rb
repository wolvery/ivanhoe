class Game < ActiveRecord::Base
  set_table_name "game"   
  
  validates_length_of :name, :within => 1..100
  
end
