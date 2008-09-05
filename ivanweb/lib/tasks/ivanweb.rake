require 'ftools'

namespace :ivanweb do
  
  desc "Update the installed Wordpress theme"
  task :update_theme do
    copy_dir( "#{RAILS_ROOT}/wordpress/blix", "#{RAILS_ROOT}/public/wp/wp-content/themes/blix" )
  end
  
  desc "Install Wordpress theme"
  task :install_theme do    
    # install php files
    Dir.mkdir("#{RAILS_ROOT}/public/wp/wp-content/themes/blix")
    copy_dir( "#{RAILS_ROOT}/wordpress/blix", "#{RAILS_ROOT}/public/wp/wp-content/themes/blix" );    
  end
    
  def copy_dir( start_dir, dest_dir )
     puts "Copying the contents of #{start_dir} to #{dest_dir}..."
     Dir.new(start_dir).each { |file|
       unless file =~ /\A\./
         start_file = "#{start_dir}/#{file}"
         dest_file = "#{dest_dir}/#{file}"  
         File.copy("#{start_dir}/#{file}", "#{dest_dir}/#{file}")
       end     
     }    
  end
end

