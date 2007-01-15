# Methods added to this helper will be available to all templates in the application.
module ApplicationHelper

def admin_status( user )
  if( user.admin )
    "You are an admin. "
  end
end

end
